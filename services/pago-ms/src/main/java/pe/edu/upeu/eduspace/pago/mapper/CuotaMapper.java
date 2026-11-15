package pe.edu.upeu.eduspace.pago.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.pago.dto.CuotaResponse;
import pe.edu.upeu.eduspace.pago.entity.Cuota;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class CuotaMapper {

    public CuotaResponse toResponse(Cuota c) {
        if (c == null) return null;
        LocalDate hoy = LocalDate.now();
        long diasAtraso = (c.getFechaVencimiento() != null && c.getFechaVencimiento().isBefore(hoy))
            ? ChronoUnit.DAYS.between(c.getFechaVencimiento(), hoy) : 0;

        return CuotaResponse.builder()
            .id(c.getId())
            .matriculaId(c.getMatricula() != null ? c.getMatricula().getMatriculaId() : null)
            .alumnoId(c.getAlumnoId())
            .anioId(c.getAnioId())
            .conceptoId(c.getConcepto() != null ? c.getConcepto().getId() : null)
            .conceptoNombre(c.getConcepto() != null ? c.getConcepto().getNombre() : null)
            .conceptoTipo(c.getConcepto() != null ? c.getConcepto().getTipo() : null)
            .descripcion(c.getDescripcion())
            .numeroCuota(c.getNumeroCuota())
            .montoBase(c.getMontoBase())
            .descuento(c.getDescuento())
            .mora(c.getMora())
            .montoTotal(c.getMontoTotal())
            .saldo(c.getSaldo())
            .fechaVencimiento(c.getFechaVencimiento())
            .estado(c.getEstado())
            .diasAtraso(diasAtraso)
            .avisoEnviado(c.getAvisoEnviado())
            .build();
    }
}
