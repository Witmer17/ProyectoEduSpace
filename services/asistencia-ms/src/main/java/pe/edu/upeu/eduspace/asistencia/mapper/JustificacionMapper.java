package pe.edu.upeu.eduspace.asistencia.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.asistencia.dto.Dtos.JustificacionResponse;
import pe.edu.upeu.eduspace.asistencia.entity.Justificacion;

@Component
public class JustificacionMapper {

    public JustificacionResponse toResponse(Justificacion j, int convertidas) {
        if (j == null) return null;
        return new JustificacionResponse(
            j.getId(), j.getMatriculaId(), j.getAlumnoId(), j.getFechaDesde(), j.getFechaHasta(),
            j.getTipo(), j.getMotivo(), j.getRutaArchivo(), j.getEstado(), j.getSolicitadoPor(), j.getResueltoPor(),
            j.getResueltoEn(), j.getComentarioResolucion(), j.getCreadoEn(), convertidas
        );
    }
}
