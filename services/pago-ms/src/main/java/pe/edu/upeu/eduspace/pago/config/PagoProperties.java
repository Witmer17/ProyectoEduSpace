package pe.edu.upeu.eduspace.pago.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.pago")
public class PagoProperties {

    private BigDecimal moraPorcentajeMensual = new BigDecimal("2.00");
    private String moraBase = "SALDO";
    private int diasAvisoPrevio = 3;
    private int diasVigenciaPrematricula = 7;
    private int margenGraciaDias = 0;
    /** Descuento (%) sobre la pensión cuando se elige pagar por semestre. */
    private BigDecimal descuentoPlanSemestral = new BigDecimal("5");
    /** Descuento (%) sobre la pensión cuando se elige pagar el año completo. */
    private BigDecimal descuentoPlanAnual = new BigDecimal("10");
    /** Minutos que vive una orden de pago antes de expirar. */
    private int minutosVigenciaOrden = 30;
    /** Secreto compartido con la pasarela para firmar sus avisos (en producción viene de una variable de entorno). */
    private String pasarelaSecreto = "dev-secreto-pasarela-simulada";
    /** Mercado Pago (pasarela real). Sin access token la pasarela real queda desactivada y se usa la simulada. */
    private MercadoPago mercadopago = new MercadoPago();

    @Getter
    @Setter
    public static class MercadoPago {
        /** Access token de la cuenta (APP_USR-…). Secreto: viene de MERCADOPAGO_ACCESS_TOKEN, nunca del repositorio. */
        private String accessToken = "";
        /** Clave pública (APP_USR-uuid). Se expone al frontend; no sirve para llamar a la API. */
        private String publicKey = "";
        /** Secreto de la firma x-signature de los avisos (panel de Mercado Pago → Webhooks). Opcional: sin él se confía en la consulta al API. */
        private String webhookSecret = "";
        private String apiBase = "https://api.mercadopago.com";
        /** URL pública del servidor (HTTPS) a la que Mercado Pago envía los avisos. Ej.: https://abc.ngrok.app */
        private String publicBaseUrl = "";
        /** Dónde vuelve la familia tras pagar (la web). */
        private String frontendUrl = "http://localhost:4200";
        private String moneda = "PEN";

        public boolean habilitado() {
            return accessToken != null && !accessToken.isBlank();
        }

        /** Modo de la cuenta según el prefijo del token: TEST- (pruebas) o APP_USR- (la cuenta; puede ser de prueba o real). */
        public String modo() {
            if (!habilitado()) return "DESACTIVADA";
            return accessToken.startsWith("TEST-") ? "PRUEBA" : "CUENTA";
        }
    }
}
