package pe.edu.upeu.eduspace.auth.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.auth.dto.Dtos.UsuarioResponse;
import pe.edu.upeu.eduspace.auth.entity.Rol;
import pe.edu.upeu.eduspace.auth.entity.Usuario;

@Component
public class UsuarioMapper {

    public UsuarioResponse toResponse(Usuario u) {
        if (u == null) return null;
        return new UsuarioResponse(
            u.getId(),
            u.getUsername(),
            u.getEmail(),
            u.getNombres(),
            u.getApellidos(),
            Boolean.TRUE.equals(u.getActivo()),
            u.estaBloqueado(),
            Boolean.TRUE.equals(u.getDebeCambiarClave()),
            u.getRoles().stream().map(Rol::getNombre).sorted().toList(),
            u.getUltimoLogin(),
            u.getCreadoEn()
        );
    }
}
