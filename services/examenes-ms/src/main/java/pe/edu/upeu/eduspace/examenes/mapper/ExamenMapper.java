package pe.edu.upeu.eduspace.examenes.mapper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.examenes.dto.ExamenDetalleResponse;
import pe.edu.upeu.eduspace.examenes.dto.ExamenDisponibleResponse;
import pe.edu.upeu.eduspace.examenes.dto.ExamenResponse;
import pe.edu.upeu.eduspace.examenes.dto.OpcionResponse;
import pe.edu.upeu.eduspace.examenes.entity.Examen;
import pe.edu.upeu.eduspace.examenes.entity.ExamenPregunta;
import pe.edu.upeu.eduspace.examenes.entity.Pregunta;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ExamenMapper {

    private final PreguntaMapper preguntaMapper;

    public ExamenResponse toResponse(Examen e) {
        if (e == null) return null;
        return ExamenResponse.builder()
            .id(e.getId())
            .anioId(e.getAnioId())
            .periodoId(e.getPeriodoId())
            .seccionId(e.getSeccionId())
            .cursoId(e.getCursoId())
            .docenteId(e.getDocenteId())
            .titulo(e.getTitulo())
            .instrucciones(e.getInstrucciones())
            .fechaInicio(e.getFechaInicio())
            .fechaFin(e.getFechaFin())
            .duracionMin(e.getDuracionMin())
            .intentosMax(e.getIntentosMax())
            .barajarPreguntas(e.getBarajarPreguntas())
            .puntajeTotal(e.getPuntajeTotal())
            .estado(e.getEstado())
            .creadoEn(e.getCreadoEn())
            .publicadoEn(e.getPublicadoEn())
            .build();
    }

    public ExamenDetalleResponse toDetalleResponse(Examen e) {
        if (e == null) return null;

        List<ExamenDetalleResponse.PreguntaItemDetalle> preguntasDetalle = new ArrayList<>();
        if (e.getPreguntas() != null) {
            for (ExamenPregunta ep : e.getPreguntas()) {
                Pregunta p = ep.getPregunta();
                List<OpcionResponse> opciones = (p.getOpciones() != null)
                    ? p.getOpciones().stream().map(preguntaMapper::toOpcionResponse).toList()
                    : new ArrayList<>();

                preguntasDetalle.add(ExamenDetalleResponse.PreguntaItemDetalle.builder()
                    .preguntaId(p.getId())
                    .orden(ep.getOrden())
                    .puntaje(ep.getPuntaje())
                    .tema(p.getTema())
                    .tipo(p.getTipo())
                    .enunciado(p.getEnunciado())
                    .opciones(opciones)
                    .build());
            }
        }

        return ExamenDetalleResponse.builder()
            .id(e.getId())
            .anioId(e.getAnioId())
            .periodoId(e.getPeriodoId())
            .seccionId(e.getSeccionId())
            .cursoId(e.getCursoId())
            .docenteId(e.getDocenteId())
            .titulo(e.getTitulo())
            .instrucciones(e.getInstrucciones())
            .fechaInicio(e.getFechaInicio())
            .fechaFin(e.getFechaFin())
            .duracionMin(e.getDuracionMin())
            .intentosMax(e.getIntentosMax())
            .barajarPreguntas(e.getBarajarPreguntas())
            .puntajeTotal(e.getPuntajeTotal())
            .estado(e.getEstado())
            .preguntas(preguntasDetalle)
            .build();
    }

    public ExamenDisponibleResponse toDisponibleResponse(Examen e, short intentosUsados, boolean puedeRendir) {
        if (e == null) return null;
        return ExamenDisponibleResponse.builder()
            .examenId(e.getId())
            .cursoId(e.getCursoId())
            .titulo(e.getTitulo())
            .instrucciones(e.getInstrucciones())
            .fechaInicio(e.getFechaInicio())
            .fechaFin(e.getFechaFin())
            .duracionMin(e.getDuracionMin())
            .intentosMax(e.getIntentosMax())
            .intentosRealizados(intentosUsados)
            .puedeRendir(puedeRendir)
            .puntajeTotal(e.getPuntajeTotal())
            .build();
    }
}
