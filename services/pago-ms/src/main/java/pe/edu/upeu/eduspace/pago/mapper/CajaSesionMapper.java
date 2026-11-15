package pe.edu.upeu.eduspace.pago.mapper;

import org.springframework.stereotype.Component;
import pe.edu.upeu.eduspace.pago.dto.CajaResponse;
import pe.edu.upeu.eduspace.pago.entity.CajaSesion;

@Component
public class CajaSesionMapper {

    public CajaResponse toResponse(CajaSesion c) {
        if (c == null) return null;
        return CajaResponse.builder()
            .id(c.getId())
            .cajeroUsuarioId(c.getCajeroUsuarioId())
            .aperturaEn(c.getAperturaEn())
            .montoApertura(c.getMontoApertura())
            .cierreEn(c.getCierreEn())
            .montoCierreDeclarado(c.getMontoCierreDeclarado())
            .montoCierreSistema(c.getMontoCierreSistema())
            .diferencia(c.getDiferencia())
            .estado(c.getEstado())
            .build();
    }
}
