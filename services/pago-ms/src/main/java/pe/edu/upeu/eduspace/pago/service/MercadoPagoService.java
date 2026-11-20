package pe.edu.upeu.eduspace.pago.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import pe.edu.upeu.eduspace.pago.client.AlumnosGateway;
import pe.edu.upeu.eduspace.pago.config.MercadoPagoProperties;
import pe.edu.upeu.eduspace.pago.dto.*;
import pe.edu.upeu.eduspace.pago.entity.Cuota;
import pe.edu.upeu.eduspace.pago.exception.BusinessRuleException;
import pe.edu.upeu.eduspace.pago.repository.CuotaRepository;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class MercadoPagoService {

    private final MercadoPagoProperties mpProps;
    private final PagoService pagoService;
    private final AlumnosGateway alumnosGateway;
    private final CuotaRepository cuotaRepository;
    private final ObjectMapper objectMapper;

    private static final String MP_API_URL = "https://api.mercadopago.com";

    public boolean tieneTokenConfigurado() {
        return mpProps.getAccessToken() != null && !mpProps.getAccessToken().isBlank();
    }

    public boolean esProduccion() {
        return mpProps.isProduccion();
    }

    /**
     * Crea una preferencia de pago en Mercado Pago Checkout Pro para las cuotas seleccionadas.
     */
    public PreferenciaMpResponse crearPreferencia(CrearPreferenciaMpRequest req, Long apoderadoUsuarioId) {
        if (mpProps.getAccessToken() == null || mpProps.getAccessToken().isBlank()) {
            throw new BusinessRuleException("El servicio de Mercado Pago no está configurado (falta MERCADOPAGO_ACCESS_TOKEN).");
        }

        // 1. Validar que las cuotas pertenezcan a un hijo del apoderado
        validarPertenencia(req.getAlumnoId(), req.getDetalles(), apoderadoUsuarioId);

        // 2. Construir ítems de Mercado Pago y calcular total
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (PagoDetalleRequest det : req.getDetalles()) {
            Cuota cuota = cuotaRepository.findById(det.getCuotaId())
                .orElseThrow(() -> new BusinessRuleException("Cuota no encontrada: " + det.getCuotaId()));

            if (!"PENDIENTE".equals(cuota.getEstado()) && !"PARCIAL".equals(cuota.getEstado()) && !"VENCIDA".equals(cuota.getEstado())) {
                throw new BusinessRuleException("La cuota " + cuota.getId() + " ya no admite cobros (estado: " + cuota.getEstado() + ")");
            }

            BigDecimal monto = det.getMontoAplicado();
            total = total.add(monto);

            Map<String, Object> item = new HashMap<>();
            item.put("id", String.valueOf(cuota.getId()));
            item.put("title", "Cuota #" + cuota.getNumeroCuota() + " - " + (cuota.getDescripcion() != null ? cuota.getDescripcion() : "Cobro escolar"));
            item.put("quantity", 1);
            item.put("currency_id", mpProps.getCurrencyId());
            item.put("unit_price", monto.doubleValue());
            items.add(item);
        }

        // 3. Serializar detalles para recuperarlos en el webhook
        String cuotasJson;
        try {
            cuotasJson = objectMapper.writeValueAsString(req.getDetalles());
        } catch (Exception e) {
            throw new RuntimeException("Error al serializar detalles de pago", e);
        }

        String externalRef = "EDUSPACE-APOD-" + apoderadoUsuarioId + "-ALU-" + req.getAlumnoId() + "-" + System.currentTimeMillis();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("alumno_id", req.getAlumnoId());
        metadata.put("apoderado_id", apoderadoUsuarioId);
        metadata.put("tipo_comprobante", req.getTipoComprobante());
        metadata.put("cuotas_json", cuotasJson);

        Map<String, Object> preferenceBody = new HashMap<>();
        preferenceBody.put("items", items);
        preferenceBody.put("external_reference", externalRef);
        preferenceBody.put("metadata", metadata);

        String baseBack = mpProps.getBackUrl();
        if (baseBack != null && !baseBack.isBlank()) {
            Map<String, String> backUrls = new HashMap<>();
            backUrls.put("success", baseBack);
            backUrls.put("pending", baseBack);
            backUrls.put("failure", baseBack);
            preferenceBody.put("back_urls", backUrls);
            if (baseBack.startsWith("https://")) {
                preferenceBody.put("auto_return", "approved");
            }
        }

        if (mpProps.getNotificationUrl() != null && !mpProps.getNotificationUrl().isBlank()) {
            preferenceBody.put("notification_url", mpProps.getNotificationUrl());
        }

        // 4. Invocar API de Mercado Pago
        RestClient restClient = RestClient.builder().baseUrl(MP_API_URL).build();
        Map<?, ?> mpResponse;
        try {
            mpResponse = restClient.post()
                .uri("/checkout/preferences")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mpProps.getAccessToken().trim())
                .contentType(MediaType.APPLICATION_JSON)
                .body(preferenceBody)
                .retrieve()
                .body(Map.class);
        } catch (Exception e) {
            log.error("Error al comunicarse con la API de Mercado Pago: {}", e.getMessage(), e);
            throw new BusinessRuleException("No se pudo iniciar el proceso de pago con Mercado Pago: " + e.getMessage());
        }

        if (mpResponse == null) {
            throw new BusinessRuleException("Mercado Pago no devolvió respuesta.");
        }

        String prefId = (String) mpResponse.get("id");
        String initPoint = (String) mpResponse.get("init_point");
        String sandboxInitPoint = (String) mpResponse.get("sandbox_init_point");

        // Si produccion=false, usamos sandbox_init_point para pruebas seguras
        String urlEfectiva = mpProps.isProduccion() ? initPoint : (sandboxInitPoint != null ? sandboxInitPoint : initPoint);

        log.info("Preferencia de Mercado Pago creada: id={}, total={}, externalRef={}, produccion={}",
            prefId, total, externalRef, mpProps.isProduccion());

        return PreferenciaMpResponse.builder()
            .preferenceId(prefId)
            .initPoint(urlEfectiva)
            .sandboxInitPoint(sandboxInitPoint)
            .externalReference(externalRef)
            .montoTotal(total)
            .publicKey(mpProps.getPublicKey())
            .build();
    }

    /**
     * Procesa la notificación Webhook enviada por Mercado Pago.
     */
    @Transactional
    public void procesarWebhook(String topic, Long paymentId) {
        if (paymentId == null) {
            log.warn("Webhook de Mercado Pago recibido sin ID de pago");
            return;
        }

        log.info("Procesando webhook de Mercado Pago para paymentId: {}, topic: {}", paymentId, topic);

        // Consultar el estado del pago en Mercado Pago
        RestClient restClient = RestClient.builder().baseUrl(MP_API_URL).build();
        Map<?, ?> paymentData;
        try {
            paymentData = restClient.get()
                .uri("/v1/payments/" + paymentId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mpProps.getAccessToken().trim())
                .retrieve()
                .body(Map.class);
        } catch (Exception e) {
            log.error("Error al consultar pago {} en Mercado Pago: {}", paymentId, e.getMessage());
            return;
        }

        if (paymentData == null) {
            log.warn("Mercado Pago no devolvió información para el pago {}", paymentId);
            return;
        }

        String status = (String) paymentData.get("status");
        log.info("Estado del pago {} en Mercado Pago: {}", paymentId, status);

        if (!"approved".equalsIgnoreCase(status)) {
            log.info("El pago {} no está aprobado (estado actual: {}), se omite registro.", paymentId, status);
            return;
        }

        // Extraer metadata
        Object metadataObj = paymentData.get("metadata");
        if (!(metadataObj instanceof Map<?, ?> meta)) {
            log.warn("El pago {} no contiene metadata para asociar en EduSpace", paymentId);
            return;
        }

        Long alumnoId = meta.get("alumno_id") != null ? Long.valueOf(meta.get("alumno_id").toString()) : null;
        Long apoderadoId = meta.get("apoderado_id") != null ? Long.valueOf(meta.get("apoderado_id").toString()) : null;
        String tipoComprobante = meta.get("tipo_comprobante") != null ? meta.get("tipo_comprobante").toString() : "RECIBO";
        String cuotasJson = meta.get("cuotas_json") != null ? meta.get("cuotas_json").toString() : null;

        if (alumnoId == null || apoderadoId == null || cuotasJson == null) {
            log.warn("Metadata incompleta en pago {}: alumno={}, apoderado={}", paymentId, alumnoId, apoderadoId);
            return;
        }

        List<PagoDetalleRequest> detalles;
        try {
            detalles = objectMapper.readValue(cuotasJson, new TypeReference<List<PagoDetalleRequest>>() {});
        } catch (Exception e) {
            log.error("Error al deserializar cuotasJson en webhook: {}", e.getMessage());
            return;
        }

        BigDecimal montoTotal = detalles.stream()
            .map(PagoDetalleRequest::getMontoAplicado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        RegistrarPagoRequest pagoReq = new RegistrarPagoRequest();
        pagoReq.setAlumnoId(alumnoId);
        pagoReq.setMontoTotal(montoTotal);
        pagoReq.setMedio("MERCADO_PAGO");
        pagoReq.setReferencia("MP-PAYMENT-" + paymentId);
        pagoReq.setIdempotencyKey("MP-TX-" + paymentId);
        pagoReq.setTipoComprobante(tipoComprobante);
        pagoReq.setDetalles(detalles);

        try {
            PagoResponse resp = pagoService.registrarPago(pagoReq, apoderadoId);
            log.info("Pago confirmado exitosamente desde Mercado Pago: id={}, comprobante={}", resp.getId(), resp.getComprobante());
        } catch (Exception e) {
            log.error("Error al registrar pago desde webhook de Mercado Pago: {}", e.getMessage(), e);
        }
    }

    private void validarPertenencia(Long alumnoId, List<PagoDetalleRequest> detalles, Long apoderadoUsuarioId) {
        boolean esHijo = alumnosGateway.hijos(apoderadoUsuarioId).stream()
            .anyMatch(h -> h.getAlumnoId().equals(alumnoId));

        boolean cuotasPropias = detalles.stream()
            .allMatch(d -> cuotaRepository.findById(d.getCuotaId()).map(c -> alumnoId.equals(c.getAlumnoId())).orElse(true));

        if (!esHijo || !cuotasPropias) {
            throw new AccessDeniedException("Solo puede pagar cuotas de sus propios hijos.");
        }
    }
}
