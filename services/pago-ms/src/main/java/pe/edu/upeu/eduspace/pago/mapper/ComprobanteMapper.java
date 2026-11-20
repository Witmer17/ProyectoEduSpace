package pe.edu.upeu.eduspace.pago.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.pago.dto.ComprobanteResponse;
import pe.edu.upeu.eduspace.pago.entity.Comprobante;

@Component
public class ComprobanteMapper {

    public ComprobanteResponse toResponse(Comprobante c) {
        if (c == null) return null;
        return ComprobanteResponse.builder()
            .id(c.getId())
            .pagoId(c.getPago() != null ? c.getPago().getId() : null)
            .tipo(c.getTipo())
            .serie(c.getSerie())
            .numero(c.getNumero())
            .numeroCompleto(String.format("%s-%08d", c.getSerie(), c.getNumero()))
            .total(c.getTotal())
            .estado(c.getEstado())
            .rutaPdf(c.getRutaPdf())
            .emitidoEn(c.getEmitidoEn())
            .build();
    }
}
