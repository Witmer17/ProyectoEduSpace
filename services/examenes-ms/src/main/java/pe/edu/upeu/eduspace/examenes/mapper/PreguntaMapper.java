package pe.edu.upeu.eduspace.examenes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.examenes.dto.OpcionResponse;
import pe.edu.upeu.eduspace.examenes.dto.PreguntaResponse;
import pe.edu.upeu.eduspace.examenes.entity.OpcionPregunta;
import pe.edu.upeu.eduspace.examenes.entity.Pregunta;

import java.util.ArrayList;
import java.util.List;

@Component
public class PreguntaMapper {

    public PreguntaResponse toResponse(Pregunta p) {
        if (p == null) return null;

        List<OpcionResponse> opcionesResp = new ArrayList<>();
        if (p.getOpciones() != null) {
            opcionesResp = p.getOpciones().stream()
                .map(this::toOpcionResponse)
                .toList();
        }

        return PreguntaResponse.builder()
            .id(p.getId())
            .cursoId(p.getCursoId())
            .docenteId(p.getDocenteId())
            .tema(p.getTema())
            .tipo(p.getTipo())
            .enunciado(p.getEnunciado())
            .puntaje(p.getPuntaje())
            .activa(p.getActiva())
            .creadaEn(p.getCreadaEn())
            .opciones(opcionesResp)
            .build();
    }

    public OpcionResponse toOpcionResponse(OpcionPregunta o) {
        if (o == null) return null;
        return OpcionResponse.builder()
            .id(o.getId())
            .texto(o.getTexto())
            .esCorrecta(o.getEsCorrecta())
            .orden(o.getOrden())
            .build();
    }
}
