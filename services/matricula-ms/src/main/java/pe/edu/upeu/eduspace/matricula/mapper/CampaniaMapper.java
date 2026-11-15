package pe.edu.upeu.eduspace.matricula.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.matricula.dto.Dtos.CampaniaResponse;
import pe.edu.upeu.eduspace.matricula.entity.CampaniaMatricula;

@Component
public class CampaniaMapper {

    public CampaniaResponse toResponse(CampaniaMatricula c) {
        if (c == null) return null;
        return new CampaniaResponse(c.getId(), c.getAnioId(), c.getNombre(), c.getFechaInicio(), c.getFechaFin(), c.getEstado());
    }
}
