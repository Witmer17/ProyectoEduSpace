package pe.edu.upeu.eduspace.matricula.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.matricula.dto.Dtos.CupoResponse;
import pe.edu.upeu.eduspace.matricula.entity.CupoSeccion;

@Component
public class CupoMapper {

    public CupoResponse toResponse(CupoSeccion c) {
        if (c == null) return null;
        return new CupoResponse(
            c.getSeccionId(),
            c.getSeccionNombre(),
            c.getAnioId(),
            c.getGradoId(),
            c.getCapacidad(),
            c.getOcupados(),
            c.getCapacidad() - c.getOcupados()
        );
    }
}
