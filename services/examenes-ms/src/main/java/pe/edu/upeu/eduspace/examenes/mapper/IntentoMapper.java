package pe.edu.upeu.eduspace.examenes.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.examenes.dto.IntentoIniciadoResponse;
import pe.edu.upeu.eduspace.examenes.dto.IntentoResultadoResponse;
import pe.edu.upeu.eduspace.examenes.entity.Examen;
import pe.edu.upeu.eduspace.examenes.entity.Intento;

import java.util.List;

@Component
public class IntentoMapper {

    public IntentoResultadoResponse toResultadoResponse(Intento intento, boolean requiereRevision) {
        if (intento == null) return null;
        Examen examen = intento.getExamen();
        return IntentoResultadoResponse.builder()
            .intentoId(intento.getId())
            .examenId(examen != null ? examen.getId() : null)
            .tituloExamen(examen != null ? examen.getTitulo() : null)
            .numeroIntento(intento.getNumero())
            .estado(intento.getEstado())
            .puntajeObtenido(intento.getPuntajeObtenido())
            .puntajeTotal(examen != null ? examen.getPuntajeTotal() : null)
            .nota(intento.getNota())
            .iniciadoEn(intento.getIniciadoEn())
            .enviadoEn(intento.getEnviadoEn())
            .calificadoEn(intento.getCalificadoEn())
            .requiereRevisionAbiertas(requiereRevision)
            .build();
    }

    public IntentoIniciadoResponse toIniciadoResponse(Intento intento, Examen examen,
                                                     List<IntentoIniciadoResponse.PreguntaEstudianteDto> preguntas) {
        if (intento == null) return null;
        return IntentoIniciadoResponse.builder()
            .intentoId(intento.getId())
            .examenId(examen != null ? examen.getId() : null)
            .tituloExamen(examen != null ? examen.getTitulo() : null)
            .numeroIntento(intento.getNumero())
            .iniciadoEn(intento.getIniciadoEn())
            .venceEn(intento.getVenceEn())
            .puntajeTotal(examen != null ? examen.getPuntajeTotal() : null)
            .preguntas(preguntas)
            .build();
    }
}
