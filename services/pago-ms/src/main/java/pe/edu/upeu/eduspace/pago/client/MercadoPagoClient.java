package pe.edu.upeu.eduspace.pago.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pe.edu.upeu.eduspace.pago.config.PagoProperties;
import pe.edu.upeu.eduspace.pago.exception.BusinessRuleException;

/**
 * Cliente de la API de Mercado Pago (Checkout Pro). Solo usa el access token del servidor: la clave pública no sirve para esta API.
 * Las respuestas se leen como mapas para no depender del orden ni de campos nuevos que Mercado Pago agregue.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MercadoPagoClient {

    private final PagoProperties props;

    /** Preferencia de pago creada: el id y la URL a la que se redirige a la familia. */
    public record Preferencia(String id, String initPoint) {}

    /** Lo que Mercado Pago informa de un pago: su estado y los datos que cruzamos con la orden. */
    public record Pago(long id, String estado, String estadoDetalle, String referenciaExterna, BigDecimal monto, String moneda,
                       String tipo, String metodo, String pagador) {}

    private RestClient cliente() {
        if (!props.getMercadopago().habilitado()) {
            throw new BusinessRuleException("La pasarela Mercado Pago no está configurada en el servidor.");
        }
        return RestClient.builder()
            .baseUrl(props.getMercadopago().getApiBase())
            .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getMercadopago().getAccessToken())
            .build();
    }

    /**
     * Crea la preferencia de Checkout Pro para una orden. {@code external_reference} es el código de la orden: así el aviso de pago
     * vuelve a enlazarse con ella.
     */
    public Preferencia crearPreferencia(String ordenCodigo, BigDecimal monto, String titulo, String pagadorNombre,
                                        String urlAviso, String urlVuelta) {
        Map<String, Object> cuerpo = new HashMap<>();
        cuerpo.put("items", List.of(Map.of("title", titulo, "quantity", 1, "unit_price", monto, "currency_id", props.getMercadopago().getMoneda())));
        cuerpo.put("external_reference", ordenCodigo);
        cuerpo.put("back_urls", Map.of("success", urlVuelta, "pending", urlVuelta, "failure", urlVuelta));
        cuerpo.put("auto_return", "approved");
        cuerpo.put("statement_descriptor", "EDUSPACE");
        cuerpo.put("payer", Map.of("name", pagadorNombre == null ? "Apoderado" : pagadorNombre));
        if (urlAviso != null) cuerpo.put("notification_url", urlAviso);   // sin URL pública no llegan avisos: se consulta con sincronizar()
        Map<?, ?> r = cliente().post().uri("/checkout/preferences")
            .contentType(MediaType.APPLICATION_JSON)
            .body(cuerpo)
            .retrieve()
            .body(Map.class);
        if (r == null || r.get("id") == null || r.get("init_point") == null) {
            throw new IllegalStateException("Mercado Pago no devolvió la preferencia de pago.");
        }
        return new Preferencia(String.valueOf(r.get("id")), String.valueOf(r.get("init_point")));
    }

    /**
     * Solo para LECTURAS (GET): si Mercado Pago limita el ritmo (429) o falla temporalmente (5xx) se reintenta UNA vez tras una pausa corta.
     * Las creaciones no se reintentan: repetirlas podría duplicar un cobro.
     */
    private <T> T lectura(java.util.function.Supplier<T> consulta) {
        try {
            return consulta.get();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() != 429 && e.getStatusCode().value() < 500) throw e;
            log.warn("Mercado Pago respondió {} en una consulta; se reintenta una vez.", e.getStatusCode().value());
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                throw e;
            }
            return consulta.get();
        }
    }

    /** Consulta el pago al propio API: el aviso solo dice el id; el estado y el monto se piden aquí, no se confían en el aviso. */
    public Pago obtenerPago(long paymentId) {
        Map<?, ?> r = lectura(() -> cliente().get().uri("/v1/payments/{id}", paymentId).retrieve().body(Map.class));
        if (r == null) throw new IllegalStateException("Mercado Pago no devolvió el pago " + paymentId);
        return mapear(r);
    }

    /** Todos los pagos que Mercado Pago tiene de una orden (por su external_reference), del más reciente al más antiguo. */
    public List<Pago> buscarPagos(String ordenCodigo) {
        Map<?, ?> r = lectura(() -> cliente().get()
            .uri(u -> u.path("/v1/payments/search").queryParam("external_reference", ordenCodigo)
                .queryParam("sort", "date_created").queryParam("criteria", "desc").build())
            .retrieve().body(Map.class));
        List<Pago> pagos = new ArrayList<>();
        if (r != null && r.get("results") instanceof List<?> lista) {
            for (Object o : lista) {
                if (o instanceof Map<?, ?> m && m.get("id") != null) pagos.add(mapear(m));
            }
        }
        return pagos;
    }

    private static Pago mapear(Map<?, ?> r) {
        Map<?, ?> pagador = r.get("payer") instanceof Map<?, ?> p ? p : Map.of();
        String nombre = (pagador.get("first_name") == null ? "" : pagador.get("first_name") + " ")
            + (pagador.get("last_name") == null ? "" : String.valueOf(pagador.get("last_name")));
        BigDecimal monto = r.get("transaction_amount") == null ? null : new BigDecimal(String.valueOf(r.get("transaction_amount")));
        return new Pago(
            Long.parseLong(String.valueOf(r.get("id"))),
            texto(r.get("status")),
            texto(r.get("status_detail")),
            texto(r.get("external_reference")),
            monto,
            texto(r.get("currency_id")),
            texto(r.get("payment_type_id")),
            texto(r.get("payment_method_id")),
            nombre.isBlank() ? null : nombre.trim());
    }

    private static String texto(Object o) {
        return o == null ? null : String.valueOf(o);
    }
}
