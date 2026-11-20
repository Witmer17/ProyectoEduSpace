package pe.edu.upeu.eduspace.pago.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.pago.dto.ConceptoResponse;
import pe.edu.upeu.eduspace.pago.entity.ConceptoPago;

@Component
public class ConceptoPagoMapper {

    public ConceptoResponse toResponse(ConceptoPago c) {
        if (c == null) return null;
        return ConceptoResponse.builder()
            .id(c.getId())
            .codigo(c.getCodigo())
            .nombre(c.getNombre())
            .tipo(c.getTipo())
            .activo(c.getActivo())
            .build();
    }
}
