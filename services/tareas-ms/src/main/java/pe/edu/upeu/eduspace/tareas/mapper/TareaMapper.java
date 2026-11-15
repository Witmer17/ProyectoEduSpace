package pe.edu.upeu.eduspace.tareas.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.tareas.dto.Dtos.AdjuntoResponse;
import pe.edu.upeu.eduspace.tareas.dto.Dtos.TareaResponse;
import pe.edu.upeu.eduspace.tareas.entity.Tarea;
import pe.edu.upeu.eduspace.tareas.entity.TareaAdjunto;

import java.util.ArrayList;
import java.util.List;

@Component
public class TareaMapper {

    public TareaResponse toResponse(Tarea t, List<TareaAdjunto> adjuntos, Integer entregadas, Integer calificadas, Integer pendientes) {
        if (t == null) return null;
        List<AdjuntoResponse> adjuntosResp = new ArrayList<>();
        if (adjuntos != null) {
            for (TareaAdjunto a : adjuntos) {
                adjuntosResp.add(toAdjuntoResponse(a));
            }
        }
        return new TareaResponse(
            t.getId(), t.getAnioId(), t.getPeriodoId(), t.getSeccionId(), t.getCursoId(), t.getDocenteId(),
            t.getTitulo(), t.getDescripcion(), t.getPublicadaEn(), t.getFechaEntrega(), t.getPermiteTardia(),
            t.getPuntajeMaximo(), t.getEstado(), adjuntosResp, entregadas, calificadas, pendientes
        );
    }

    public TareaResponse toResponseLiviano(Tarea t) {
        if (t == null) return null;
        return new TareaResponse(
            t.getId(), t.getAnioId(), t.getPeriodoId(), t.getSeccionId(), t.getCursoId(), t.getDocenteId(),
            t.getTitulo(), t.getDescripcion(), t.getPublicadaEn(), t.getFechaEntrega(), t.getPermiteTardia(),
            t.getPuntajeMaximo(), t.getEstado(), null, null, null, null
        );
    }

    public AdjuntoResponse toAdjuntoResponse(TareaAdjunto a) {
        if (a == null) return null;
        return new AdjuntoResponse(a.getId(), a.getNombreArchivo(), a.getContentType(), a.getTamanoBytes());
    }
}
