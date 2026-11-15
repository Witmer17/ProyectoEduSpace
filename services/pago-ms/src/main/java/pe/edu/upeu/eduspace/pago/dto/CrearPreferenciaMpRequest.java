package pe.edu.upeu.eduspace.pago.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CrearPreferenciaMpRequest {

    @NotNull(message = "El alumnoId es obligatorio")
    private Long alumnoId;

    @NotEmpty(message = "Debe incluir al menos una cuota a pagar")
    @Valid
    private List<PagoDetalleRequest> detalles;

    @Builder.Default
    private String tipoComprobante = "RECIBO";
}
