package pe.edu.upeu.eduspace.asistencia.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.asistencia.dto.Dtos.DetalleResponse;
import pe.edu.upeu.eduspace.asistencia.dto.Dtos.SesionResponse;
import pe.edu.upeu.eduspace.asistencia.entity.AsistenciaDetalle;
import pe.edu.upeu.eduspace.asistencia.entity.SesionAsistencia;

import java.util.List;

@Component
public class AsistenciaMapper {

    public DetalleResponse toDetalleResponse(AsistenciaDetalle d, String alumnoNombre) {
        if (d == null) return null;
        return new DetalleResponse(
            d.getId(), d.getMatriculaId(), d.getAlumnoId(), alumnoNombre, d.getEstado(),
            d.getMinutosTarde(), d.getObservacion(), Boolean.TRUE.equals(d.getConfirmado())
        );
    }

    public SesionResponse toSesionResponse(SesionAsistencia s, List<DetalleResponse> alumnos) {
        if (s == null) return null;
        return new SesionResponse(
            s.getId(), s.getAnioId(), s.getPeriodoId(), s.getSeccionId(), s.getFecha(), s.getTipo(),
            s.getCursoId(), s.getBloqueId(), s.getDocenteId(), s.getEstado(), s.getCerradaEn(), alumnos
        );
    }
}
