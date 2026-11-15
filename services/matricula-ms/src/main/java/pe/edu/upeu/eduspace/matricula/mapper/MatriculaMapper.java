package pe.edu.upeu.eduspace.matricula.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.matricula.dto.Dtos.*;
import pe.edu.upeu.eduspace.matricula.entity.Matricula;
import pe.edu.upeu.eduspace.matricula.entity.MatriculaHistorial;
import pe.edu.upeu.eduspace.matricula.entity.MatriculaRequisito;

@Component
public class MatriculaMapper {

    public MatriculaResponse toResponse(Matricula m) {
        if (m == null) return null;
        return new MatriculaResponse(
            m.getId(), m.getCodigo(), m.getAlumnoId(), m.getAlumnoNombre(), m.getAnioId(),
            m.getNivelId(), m.getGradoId(), m.getSeccionId(), m.getCampaniaId(), m.getTipo(), m.getEstado(),
            m.getMatriculaAnteriorId(), m.getVenceEn(), m.getObservaciones(), m.getCreadoEn(), m.getConfirmadaEn(),
            m.getCerradaEn(), m.getMotivoCierre(), m.getPagadaEn(), m.getAprobadaEn()
        );
    }

    public RequisitoResponse toRequisitoResponse(MatriculaRequisito r) {
        if (r == null) return null;
        return new RequisitoResponse(
            r.getId(), r.getTipo(), r.getCumplido(), r.getObservacion(), r.getVerificadoPor(),
            r.getVerificadoEn()
        );
    }

    public HistorialResponse toHistorialResponse(MatriculaHistorial h) {
        if (h == null) return null;
        return new HistorialResponse(
            h.getId(), h.getTipoCambio(), h.getEstadoAnterior(), h.getEstadoNuevo(),
            h.getSeccionAnteriorId(), h.getSeccionNuevaId(), h.getMotivo(), h.getUsuarioId(), h.getFecha()
        );
    }
}
