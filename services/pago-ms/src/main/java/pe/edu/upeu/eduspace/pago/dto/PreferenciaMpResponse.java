package pe.edu.upeu.eduspace.pago.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PreferenciaMpResponse {
    private String preferenceId;
    private String initPoint;
    private String sandboxInitPoint;
    private String externalReference;
    private BigDecimal montoTotal;
    private String publicKey;
}
