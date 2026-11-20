package pe.edu.upeu.eduspace.pago.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import pe.edu.upeu.eduspace.pago.dto.CrearPreferenciaMpRequest;
import pe.edu.upeu.eduspace.pago.dto.PreferenciaMpResponse;
import pe.edu.upeu.eduspace.pago.dto.RegistrarPagoRequest;
import pe.edu.upeu.eduspace.pago.security.UsuarioActual;
import pe.edu.upeu.eduspace.pago.service.MercadoPagoService;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Mercado Pago", description = "Endpoints de integración con la pasarela de pagos Mercado Pago")
public class MercadoPagoController {

    private final MercadoPagoService mercadoPagoService;

    @GetMapping("/pasarela/estado")
    @Operation(summary = "Consultar qué pasarela de pagos está activa")
    public ResponseEntity<Map<String, String>> estadoPasarela() {
        boolean activo = mercadoPagoService.tieneTokenConfigurado();
        String pasarela = activo ? "MERCADOPAGO" : "SIMULADA";
        String modo = mercadoPagoService.esProduccion() ? "PRODUCCION" : "PRUEBA";
        return ResponseEntity.ok(Map.of("pasarela", pasarela, "modo", modo));
    }

    @PostMapping("/online/mercadopago")
    @Operation(summary = "Iniciar pago online de pensiones con Mercado Pago (APODERADO)")
    public ResponseEntity<Map<String, String>> pagarOnlineMercadoPago(
            @Valid @RequestBody RegistrarPagoRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        Long apoderadoUsuarioId = UsuarioActual.id(jwt);
        CrearPreferenciaMpRequest prefReq = new CrearPreferenciaMpRequest();
        prefReq.setAlumnoId(req.getAlumnoId());
        prefReq.setTipoComprobante(req.getTipoComprobante());
        prefReq.setDetalles(req.getDetalles());
        PreferenciaMpResponse pref = mercadoPagoService.crearPreferencia(prefReq, apoderadoUsuarioId);
        String redireccion = (pref.getSandboxInitPoint() != null && !pref.getSandboxInitPoint().isBlank())
            ? pref.getSandboxInitPoint()
            : pref.getInitPoint();
        return ResponseEntity.ok(Map.of(
            "codigo", pref.getPreferenceId(),
            "redireccion", redireccion,
            "modo", "PRUEBA"
        ));
    }

    @PostMapping("/online/mercadopago/{pagoId}/sincronizar")
    @Operation(summary = "Sincronizar retorno de Mercado Pago")
    public ResponseEntity<Void> sincronizarPagoMercadoPago(@PathVariable Long pagoId) {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mercadopago/preferencia")
    @Operation(summary = "Crear preferencia de pago en Mercado Pago Checkout Pro (APODERADO)")
    public ResponseEntity<PreferenciaMpResponse> crearPreferencia(
            @Valid @RequestBody CrearPreferenciaMpRequest req,
            @AuthenticationPrincipal Jwt jwt) {
        Long apoderadoUsuarioId = UsuarioActual.id(jwt);
        PreferenciaMpResponse resp = mercadoPagoService.crearPreferencia(req, apoderadoUsuarioId);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/webhook/mercadopago")
    @Operation(summary = "Recepción de notificaciones Webhook de Mercado Pago (PÚBLICO)")
    public ResponseEntity<Void> webhookPost(
            @RequestParam(value = "topic", required = false) String topic,
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "id", required = false) Long id,
            @RequestParam(value = "data.id", required = false) Long dataId,
            @RequestBody(required = false) Map<String, Object> body) {

        String effectiveTopic = topic != null ? topic : type;
        Long paymentId = id != null ? id : dataId;

        if (paymentId == null && body != null) {
            Object dataObj = body.get("data");
            if (dataObj instanceof Map<?, ?> dataMap && dataMap.get("id") != null) {
                try {
                    paymentId = Long.valueOf(dataMap.get("id").toString());
                } catch (NumberFormatException ignored) {}
            }
            if (paymentId == null && body.get("id") != null) {
                try {
                    paymentId = Long.valueOf(body.get("id").toString());
                } catch (NumberFormatException ignored) {}
            }
            if (effectiveTopic == null && body.get("type") != null) {
                effectiveTopic = body.get("type").toString();
            }
            if (effectiveTopic == null && body.get("action") != null) {
                effectiveTopic = body.get("action").toString();
            }
        }

        if (paymentId != null) {
            mercadoPagoService.procesarWebhook(effectiveTopic, paymentId);
        }

        // Siempre responder 200 OK a Mercado Pago para confirmar la recepción
        return ResponseEntity.ok().build();
    }
}
