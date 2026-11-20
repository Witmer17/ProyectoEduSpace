package pe.edu.eduspace.planificacion.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pe.edu.eduspace.planificacion.entity.Aula;
import pe.edu.eduspace.planificacion.entity.Pabellon;
import pe.edu.eduspace.planificacion.entity.Seccion;
import pe.edu.eduspace.planificacion.entity.TipoAula;
import pe.edu.eduspace.planificacion.entity.Turno;
import pe.edu.eduspace.planificacion.exception.BusinessRuleException;
import pe.edu.eduspace.planificacion.exception.ResourceNotFoundException;
import pe.edu.eduspace.planificacion.repository.AulaRepository;
import pe.edu.eduspace.planificacion.repository.PabellonRepository;
import pe.edu.eduspace.planificacion.repository.SeccionRepository;

/**
 * Asigna aulas a las secciones del año que todavía no tienen una. Reglas:
 * <ul>
 *   <li>Solo llena secciones SIN aula: lo que ya tiene aula no se toca.</li>
 *   <li>Cada grado usa el pabellón que lo declara (p. ej. inicial en un pabellón, primaria en otro).</li>
 *   <li>El aula debe estar activa, tener capacidad para la sección y no estar ocupada por otra sección en el mismo turno.</li>
 *   <li>Si no hay aula libre para una sección, no se detiene el resto: se informa la falta por grado y pabellón, con la capacidad
 *       mínima necesaria, para que la administración agregue un aula y vuelva a ejecutar.</li>
 * </ul>
 * Entre las aulas que sirven se elige la de menor capacidad suficiente, para dejar libres las más grandes.
 */
@Service
@RequiredArgsConstructor
public class AsignacionAulasService {

    private final SeccionRepository secciones;
    private final AulaRepository aulas;
    private final PabellonRepository pabellones;

    /** Una sección y el aula que se le asignaría (null si no hay aula disponible). */
    public record Item(Long seccionId, String seccion, Long gradoId, String grado, String turno, Short capacidad,
                       Long aulaId, String aula, Long pabellonId, String pabellon, String motivo) {}

    /** Secciones que no pudieron recibir aula, agrupadas por grado y pabellón. */
    public record Falta(Long gradoId, String grado, Long pabellonId, String pabellon, int secciones, int capacidadMinima, String motivo) {}

    public record Propuesta(Long anioId, List<Item> asignaciones, List<Falta> faltas, int asignables, int sinAula) {}

    public record Resultado(int asignadas, int sinAula) {}

    @Transactional(readOnly = true)
    public Propuesta calcular(Long anioId) {
        Plan plan = planear(anioId);
        List<Item> items = plan.asignaciones().stream().map(a -> new Item(
                a.seccion().getId(), a.seccion().getNombre(), a.seccion().getGrado().getId(), a.seccion().getGrado().getNombre(),
                a.seccion().getTurno().name(), a.seccion().getCapacidad(),
                a.aula() == null ? null : a.aula().getId(), a.aula() == null ? null : a.aula().getCodigo(),
                a.pabellon() == null ? null : a.pabellon().getId(), a.pabellon() == null ? null : a.pabellon().getNombre(),
                a.motivo())).toList();
        int asignables = (int) items.stream().filter(i -> i.aulaId() != null).count();
        return new Propuesta(anioId, items, plan.faltas(), asignables, items.size() - asignables);
    }

    @Transactional
    public Resultado aplicar(Long anioId) {
        Plan plan = planear(anioId);
        int asignadas = 0;
        for (Asignacion a : plan.asignaciones()) {
            if (a.aula() == null) continue;
            a.seccion().setAula(a.aula());
            secciones.save(a.seccion());
            asignadas++;
        }
        return new Resultado(asignadas, plan.asignaciones().size() - asignadas);
    }

    /**
     * Agrega un aula al pabellón (para cubrir una falta de la asignación): crea el aula con un código libre, la deja activa con la
     * capacidad pedida y sube en uno las aulas que declara el pabellón.
     */
    @Transactional
    public Aula agregarAula(Long pabellonId, Short capacidad) {
        Pabellon pab = pabellones.findById(pabellonId)
                .orElseThrow(() -> ResourceNotFoundException.of("Pabellón", pabellonId));
        if (capacidad == null || capacidad < 1 || capacidad > 500) {
            throw new BusinessRuleException("La capacidad del aula debe estar entre 1 y 500");
        }
        String prefijo = pab.getNombre().replaceAll("[^A-Za-z0-9]", "").toUpperCase();
        if (prefijo.isEmpty()) prefijo = "AULA";
        if (prefijo.length() > 8) prefijo = prefijo.substring(0, 8);
        int n = aulas.findByPabellonIdOrderByCodigo(pabellonId).size() + 1;
        String codigo = prefijo + "-" + String.format("%02d", n);
        while (aulas.existsByCodigo(codigo)) {
            n++;
            codigo = prefijo + "-" + String.format("%02d", n);
        }
        Aula aula = new Aula();
        aula.setCodigo(codigo);
        aula.setNombre("Aula " + codigo);
        aula.setTipo(TipoAula.AULA);
        aula.setCapacidad(capacidad);
        aula.setActivo(true);
        aula.setPabellon(pab);
        Aula creada = aulas.save(aula);
        pab.setCantidadAulas((short) (pab.getCantidadAulas() + 1));
        pabellones.save(pab);
        return creada;
    }

    private Plan planear(Long anioId) {
        List<Seccion> delAnio = secciones.buscar(anioId, null).stream()
                .filter(s -> Boolean.TRUE.equals(s.getActivo()))
                .sorted(Comparator.comparing((Seccion s) -> s.getGrado().getId()).thenComparing(Seccion::getNombre))
                .toList();

        // Turnos ya ocupados por aula (las secciones que ya tienen aula cuentan como ocupación).
        Map<Long, Set<Turno>> ocupadas = new HashMap<>();
        for (Seccion s : delAnio) {
            if (s.getAula() != null) ocupadas.computeIfAbsent(s.getAula().getId(), k -> EnumSet.noneOf(Turno.class)).add(s.getTurno());
        }

        List<Aula> activas = aulas.findAll().stream().filter(a -> Boolean.TRUE.equals(a.getActivo())).toList();

        // Pabellón que usa cada grado (un pabellón declara qué grados atiende).
        Map<Long, Pabellon> pabellonDeGrado = new HashMap<>();
        for (Pabellon p : pabellones.findAll()) {
            for (Long gradoId : p.getGradoIds()) pabellonDeGrado.putIfAbsent(gradoId, p);
        }

        List<Asignacion> asignaciones = new ArrayList<>();
        Map<String, Falta> faltasPorClave = new LinkedHashMap<>();

        for (Seccion s : delAnio) {
            if (s.getAula() != null) continue;   // ya tiene aula: no se toca
            Long gradoId = s.getGrado().getId();
            String gradoNombre = s.getGrado().getNombre();
            Pabellon pab = pabellonDeGrado.get(gradoId);
            if (pab == null) {
                String motivo = "El grado " + gradoNombre + " no tiene pabellón asignado (Datos del colegio → Pabellones)";
                asignaciones.add(new Asignacion(s, null, null, motivo));
                registrarFalta(faltasPorClave, gradoId, gradoNombre, null, null, s.getCapacidad(), motivo);
                continue;
            }
            Aula elegida = aulaLibre(activas, pab, s, ocupadas);
            if (elegida == null) {
                String motivo = "No hay aula libre en el pabellón " + pab.getNombre() + " con capacidad ≥ " + s.getCapacidad()
                        + " para el turno " + s.getTurno().name();
                asignaciones.add(new Asignacion(s, null, pab, motivo));
                registrarFalta(faltasPorClave, gradoId, gradoNombre, pab.getId(), pab.getNombre(), s.getCapacidad(), motivo);
            } else {
                ocupadas.computeIfAbsent(elegida.getId(), k -> EnumSet.noneOf(Turno.class)).add(s.getTurno());
                asignaciones.add(new Asignacion(s, elegida, pab, null));
            }
        }
        return new Plan(asignaciones, new ArrayList<>(faltasPorClave.values()));
    }

    /** El aula del pabellón con capacidad suficiente y libre en ese turno; la de menor capacidad que sirva. */
    private static Aula aulaLibre(List<Aula> activas, Pabellon pab, Seccion s, Map<Long, Set<Turno>> ocupadas) {
        return activas.stream()
                .filter(a -> a.getPabellon() != null && pab.getId().equals(a.getPabellon().getId()))
                .filter(a -> a.getCapacidad() != null && a.getCapacidad() >= s.getCapacidad())
                .filter(a -> !ocupadas.getOrDefault(a.getId(), Set.of()).contains(s.getTurno()))
                .min(Comparator.comparing((Aula a) -> a.getCapacidad()).thenComparing(Aula::getCodigo))
                .orElse(null);
    }

    private static void registrarFalta(Map<String, Falta> faltas, Long gradoId, String grado, Long pabId, String pab,
                                       Short capacidad, String motivo) {
        String clave = gradoId + ":" + pabId;
        Falta previa = faltas.get(clave);
        int secciones = previa == null ? 1 : previa.secciones() + 1;
        int capacidadMinima = previa == null ? capacidad : Math.max(previa.capacidadMinima(), capacidad);
        faltas.put(clave, new Falta(gradoId, grado, pabId, pab, secciones, capacidadMinima, motivo));
    }

    private record Asignacion(Seccion seccion, Aula aula, Pabellon pabellon, String motivo) {}

    private record Plan(List<Asignacion> asignaciones, List<Falta> faltas) {}
}
