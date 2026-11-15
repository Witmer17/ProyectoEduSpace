package pe.edu.upeu.eduspace.pago.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.pago.dto.ComprobanteResponse;
import pe.edu.upeu.eduspace.pago.dto.PagoResponse;
import pe.edu.upeu.eduspace.pago.entity.Comprobante;
import pe.edu.upeu.eduspace.pago.entity.Pago;
import pe.edu.upeu.eduspace.pago.entity.PagoDetalle;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PagoMapper {

    private final ComprobanteMapper comprobanteMapper;

    public PagoResponse toResponse(Pago p) {
        return toResponse(p, null);
    }

    public PagoResponse toResponse(Pago p, Comprobante c) {
        if (p == null) return null;

        List<PagoResponse.PagoDetalleResponse> detResponses = new ArrayList<>();
        if (p.getDetalles() != null) {
            for (PagoDetalle d : p.getDetalles()) {
                detResponses.add(PagoResponse.PagoDetalleResponse.builder()
                    .id(d.getId())
                    .cuotaId(d.getCuota() != null ? d.getCuota().getId() : null)
                    .cuotaDescripcion(d.getCuota() != null ? d.getCuota().getDescripcion() : null)
                    .montoAplicado(d.getMontoAplicado())
                    .nuevoSaldoCuota(d.getCuota() != null ? d.getCuota().getSaldo() : null)
                    .nuevoEstadoCuota(d.getCuota() != null ? d.getCuota().getEstado() : null)
                    .build());
            }
        }

        ComprobanteResponse compResp = (c != null) ? comprobanteMapper.toResponse(c) : null;

        return PagoResponse.builder()
            .id(p.getId())
            .alumnoId(p.getAlumnoId())
            .pagadorNombre(p.getPagadorNombre())
            .pagadorDocumento(p.getPagadorDocumento())
            .montoTotal(p.getMontoTotal())
            .medio(p.getMedio())
            .referencia(p.getReferencia())
            .estado(p.getEstado())
            .cajaSesionId(p.getCajaSesion() != null ? p.getCajaSesion().getId() : null)
            .registradoPor(p.getRegistradoPor())
            .pagadoEn(p.getPagadoEn())
            .idempotencyKey(p.getIdempotencyKey())
            .comprobante(compResp)
            .detalles(detResponses)
            .build();
    }
}
