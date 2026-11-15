package pe.edu.upeu.eduspace.tareas.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.tareas.dto.Dtos.MaterialResponse;
import pe.edu.upeu.eduspace.tareas.entity.Material;

@Component
public class MaterialMapper {

    public MaterialResponse toResponse(Material m) {
        if (m == null) return null;
        return new MaterialResponse(
            m.getId(), m.getSeccionId(), m.getCursoId(), m.getDocenteId(), m.getTitulo(), m.getDescripcion(),
            m.getTipo(), m.getEnlace(), m.getNombreArchivo(), m.getContentType(), m.getTamanoBytes(), m.getCreadoEn()
        );
    }
}
