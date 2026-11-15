package pe.edu.upeu.eduspace.calificaciones.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.calificaciones.dto.CriterioResponse;
import pe.edu.upeu.eduspace.calificaciones.entity.CriterioEvaluacion;

@Component
public class CriterioMapper {

    public CriterioResponse toResponse(CriterioEvaluacion c) {
        if (c == null) return null;
        return CriterioResponse.builder()
            .id(c.getId())
            .anioId(c.getAnioId())
            .periodoId(c.getPeriodoId())
            .seccionId(c.getSeccionId())
            .cursoId(c.getCursoId())
            .nombre(c.getNombre())
            .peso(c.getPeso())
            .origenTipo(c.getOrigenTipo())
            .creadoPor(c.getCreadoPor())
            .creadoEn(c.getCreadoEn())
            .build();
    }
}
