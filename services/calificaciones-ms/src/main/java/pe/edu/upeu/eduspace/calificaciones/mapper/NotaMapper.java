package pe.edu.upeu.eduspace.calificaciones.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.calificaciones.dto.NotaPendienteResponse;
import pe.edu.upeu.eduspace.calificaciones.dto.NotaResponse;
import pe.edu.upeu.eduspace.calificaciones.entity.Nota;
import pe.edu.upeu.eduspace.calificaciones.entity.NotaPendiente;

@Component
public class NotaMapper {

    public NotaResponse toResponse(Nota n) {
        if (n == null) return null;
        return NotaResponse.builder()
            .id(n.getId())
            .matriculaId(n.getMatriculaId())
            .alumnoId(n.getAlumnoId())
            .seccionId(n.getSeccionId())
            .cursoId(n.getCursoId())
            .periodoId(n.getPeriodoId())
            .criterioId(n.getCriterio() != null ? n.getCriterio().getId() : null)
            .criterioNombre(n.getCriterio() != null ? n.getCriterio().getNombre() : null)
            .valor(n.getValor())
            .origen(n.getOrigen())
            .origenId(n.getOrigenId())
            .registradoPor(n.getRegistradoPor())
            .registradoEn(n.getRegistradoEn())
            .actualizadoEn(n.getActualizadoEn())
            .build();
    }

    public NotaPendienteResponse toPendienteResponse(NotaPendiente p) {
        if (p == null) return null;
        return NotaPendienteResponse.builder()
            .id(p.getId() != null ? String.valueOf(p.getId()) : null)
            .origen(p.getOrigen())
            .origenId(p.getOrigenId())
            .titulo(p.getTitulo())
            .alumnoId(p.getAlumnoId())
            .matriculaId(p.getMatriculaId())
            .seccionId(p.getSeccionId())
            .cursoId(p.getCursoId())
            .periodoId(p.getPeriodoId())
            .valor(p.getValor())
            .fechaEvento(p.getFechaEvento())
            .motivo(p.getEstado())
            .build();
    }
}
