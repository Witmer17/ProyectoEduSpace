package pe.edu.upeu.eduspace.pago.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.pago.dto.TarifaResponse;
import pe.edu.upeu.eduspace.pago.entity.Tarifa;

@Component
public class TarifaMapper {

    public TarifaResponse toResponse(Tarifa t) {
        if (t == null) return null;
        return TarifaResponse.builder()
            .id(t.getId())
            .anioId(t.getAnioId())
            .nivelId(t.getNivelId())
            .gradoId(t.getGradoId())
            .conceptoId(t.getConcepto() != null ? t.getConcepto().getId() : null)
            .conceptoCodigo(t.getConcepto() != null ? t.getConcepto().getCodigo() : null)
            .conceptoNombre(t.getConcepto() != null ? t.getConcepto().getNombre() : null)
            .monto(t.getMonto())
            .numCuotas(t.getNumCuotas())
            .mesInicio(t.getMesInicio())
            .diaVencimiento(t.getDiaVencimiento())
            .activo(t.getActivo())
            .build();
    }
}
