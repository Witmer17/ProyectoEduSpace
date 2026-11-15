package pe.edu.upeu.eduspace.pago.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "mercadopago")
public class MercadoPagoProperties {

    /** Token de acceso de Mercado Pago (empieza con APP_USR- en producción o TEST- en pruebas). */
    private String accessToken = "";

    /** Clave pública para el frontend de Mercado Pago. */
    private String publicKey = "";

    /** URL base de retorno para redireccionar al apoderado tras el pago. */
    private String backUrl = "http://localhost:4200/familia/pagos";

    /** URL de notificación para el webhook (debe ser accesible públicamente, ej. con ngrok en dev). */
    private String notificationUrl = "";

    /** Moneda oficial para el cobro (PEN por defecto para Perú). */
    private String currencyId = "PEN";

    /** Activar modo producción (false usa sandbox_init_point; true usa init_point). */
    private boolean produccion = true;
}
