package pe.edu.upeu.eduspace.pago.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.pago.dto.DescuentoResponse;
import pe.edu.upeu.eduspace.pago.entity.Descuento;

@Component
public class DescuentoMapper {

    public DescuentoResponse toResponse(Descuento d) {
        if (d == null) return null;
        return DescuentoResponse.builder()
            .id(d.getId())
            .nombre(d.getNombre())
            .tipo(d.getTipo())
            .valor(d.getValor())
            .aplicaA(d.getAplicaA())
            .activo(d.getActivo())
            .build();
    }
}
