package pe.edu.upeu.eduspace.calificaciones.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.calificaciones.dto.BoletinResponse;
import pe.edu.upeu.eduspace.calificaciones.entity.Boletin;

@Component
public class BoletinMapper {

    public BoletinResponse toResponse(Boletin b) {
        if (b == null) return null;
        return BoletinResponse.builder()
            .id(b.getId())
            .matriculaId(b.getMatriculaId())
            .alumnoId(b.getAlumnoId())
            .periodoId(b.getPeriodoId())
            .version(b.getVersion())
            .estado(b.getEstado())
            .rutaPdf(b.getRutaPdf())
            .urlDescarga("/api/v1/calificaciones/boletines/" + b.getId() + "/url")
            .generadoPor(b.getGeneradoPor())
            .generadoEn(b.getGeneradoEn())
            .build();
    }
}
