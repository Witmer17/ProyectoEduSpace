package pe.edu.upeu.eduspace.tareas.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.tareas.dto.Dtos.ArchivoResponse;
import pe.edu.upeu.eduspace.tareas.dto.Dtos.EntregaResponse;
import pe.edu.upeu.eduspace.tareas.entity.Entrega;
import pe.edu.upeu.eduspace.tareas.entity.EntregaArchivo;

import java.util.ArrayList;
import java.util.List;

@Component
public class EntregaMapper {

    public EntregaResponse toResponse(Entrega e, String alumnoNombre, List<EntregaArchivo> archivos) {
        if (e == null) return null;
        List<ArchivoResponse> archResponses = new ArrayList<>();
        if (archivos != null) {
            for (EntregaArchivo a : archivos) {
                archResponses.add(toArchivoResponse(a));
            }
        }
        return new EntregaResponse(
            e.getId(), e.getTareaId(), e.getMatriculaId(), e.getAlumnoId(), alumnoNombre, e.getComentario(),
            e.getEntregadaEn(), e.getEsTardia(), e.getVersion(), e.getEstado(), e.getNota(),
            e.getRetroalimentacion(), e.getCalificadaEn(), archResponses
        );
    }

    public ArchivoResponse toArchivoResponse(EntregaArchivo a) {
        if (a == null) return null;
        return new ArchivoResponse(a.getId(), a.getNombreArchivo(), a.getContentType(), a.getTamanoBytes());
    }
}
