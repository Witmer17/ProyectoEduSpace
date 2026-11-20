package pe.edu.eduspace.planificacion.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pe.edu.eduspace.planificacion.dto.AulaResponse;
import pe.edu.eduspace.planificacion.entity.Aula;
import pe.edu.eduspace.planificacion.service.AsignacionAulasService;

/**
 * Asignación automática de aulas a las secciones del año. GET muestra la propuesta (sin guardar nada);
 * POST la aplica. El POST exige ADMIN por la regla general de {@code /api/v1/planificacion/**}.
 */
@RestController
@RequestMapping("/api/v1/planificacion/asignacion-aulas")
@RequiredArgsConstructor
public class AsignacionAulasController {

    private final AsignacionAulasService service;

    @GetMapping
    public AsignacionAulasService.Propuesta propuesta(@RequestParam Long anioId) {
        return service.calcular(anioId);
    }

    @PostMapping
    public AsignacionAulasService.Resultado aplicar(@RequestParam Long anioId) {
        return service.aplicar(anioId);
    }

    /** Agrega un aula al pabellón de una falta (el código lo pone el servidor y el pabellón sube en uno sus aulas declaradas). */
    @PostMapping("/agregar-aula")
    public AulaResponse agregarAula(@RequestParam Long pabellonId, @RequestParam Short capacidad) {
        Aula a = service.agregarAula(pabellonId, capacidad);
        return new AulaResponse(a.getId(), a.getCodigo(), a.getNombre(), a.getTipo(), a.getCapacidad(), a.getActivo(),
                a.getPabellon().getId(), a.getPabellon().getNombre());
    }
}
