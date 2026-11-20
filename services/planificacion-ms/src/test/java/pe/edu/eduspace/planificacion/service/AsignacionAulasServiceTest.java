package pe.edu.eduspace.planificacion.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.Test;
import pe.edu.eduspace.planificacion.entity.Aula;
import pe.edu.eduspace.planificacion.entity.Grado;
import pe.edu.eduspace.planificacion.entity.Pabellon;
import pe.edu.eduspace.planificacion.entity.Seccion;
import pe.edu.eduspace.planificacion.entity.Turno;
import pe.edu.eduspace.planificacion.repository.AulaRepository;
import pe.edu.eduspace.planificacion.repository.PabellonRepository;
import pe.edu.eduspace.planificacion.repository.SeccionRepository;

class AsignacionAulasServiceTest {

    private final SeccionRepository secciones = mock(SeccionRepository.class);
    private final AulaRepository aulas = mock(AulaRepository.class);
    private final PabellonRepository pabellones = mock(PabellonRepository.class);
    private final AsignacionAulasService servicio = new AsignacionAulasService(secciones, aulas, pabellones);

    private static Aula aula(long id, String codigo, int capacidad, Pabellon pab) {
        Aula a = new Aula();
        setId(a, id);
        a.setCodigo(codigo);
        a.setNombre(codigo);
        a.setCapacidad((short) capacidad);
        a.setActivo(true);
        a.setPabellon(pab);
        return a;
    }

    private static Pabellon pabellon(long id, String nombre, Long... grados) {
        Pabellon p = new Pabellon();
        setId(p, id);
        p.setNombre(nombre);
        p.getGradoIds().addAll(List.of(grados));
        return p;
    }

    private static Grado grado(long id, String nombre) {
        Grado g = new Grado();
        setId(g, id);
        g.setNombre(nombre);
        return g;
    }

    private static Seccion seccion(long id, Grado g, String nombre, Turno turno, int capacidad, Aula aula) {
        Seccion s = new Seccion();
        setId(s, id);
        s.setGrado(g);
        s.setNombre(nombre);
        s.setTurno(turno);
        s.setCapacidad((short) capacidad);
        s.setAula(aula);
        s.setActivo(true);
        return s;
    }

    private static void setId(Object entidad, long id) {
        try {
            var campo = entidad.getClass().getDeclaredField("id");
            campo.setAccessible(true);
            campo.set(entidad, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void asignaElAulaDelPabellonDelGradoYNoTocaLasQueYaTienenAula() {
        Pabellon inicial = pabellon(1, "Inicial", 10L);
        Pabellon primaria = pabellon(2, "Primaria", 20L);
        Aula ini1 = aula(101, "I-1", 25, inicial);
        Aula pri1 = aula(201, "P-1", 35, primaria);
        Grado g3 = grado(10, "3 años");
        Grado g1 = grado(20, "1° de Primaria");
        Seccion yaTiene = seccion(1, g3, "A", Turno.MANANA, 20, ini1);
        Seccion sinAula = seccion(2, g1, "A", Turno.MANANA, 30, null);

        when(secciones.buscar(any(), any())).thenReturn(List.of(yaTiene, sinAula));
        when(aulas.findAll()).thenReturn(List.of(ini1, pri1));
        when(pabellones.findAll()).thenReturn(List.of(inicial, primaria));

        var propuesta = servicio.calcular(7L);

        assertThat(propuesta.asignables()).isEqualTo(1);
        assertThat(propuesta.sinAula()).isZero();
        assertThat(propuesta.asignaciones()).hasSize(1);
        assertThat(propuesta.asignaciones().get(0).aula()).isEqualTo("P-1");
        assertThat(propuesta.asignaciones().get(0).pabellon()).isEqualTo("Primaria");
    }

    @Test
    void noRepiteUnAulaEnElMismoTurnoPeroSiEnOtroTurno() {
        Pabellon primaria = pabellon(2, "Primaria", 20L);
        Aula pri1 = aula(201, "P-1", 35, primaria);
        Grado g1 = grado(20, "1° de Primaria");
        Seccion manana = seccion(1, g1, "A", Turno.MANANA, 30, null);
        Seccion manana2 = seccion(2, g1, "B", Turno.MANANA, 30, null);
        Seccion tarde = seccion(3, g1, "C", Turno.TARDE, 30, null);

        when(secciones.buscar(any(), any())).thenReturn(List.of(manana, manana2, tarde));
        when(aulas.findAll()).thenReturn(List.of(pri1));
        when(pabellones.findAll()).thenReturn(List.of(primaria));

        var propuesta = servicio.calcular(7L);

        assertThat(propuesta.asignables()).isEqualTo(2);     // la mañana y la tarde usan P-1; la segunda mañana no encuentra aula
        assertThat(propuesta.faltas()).hasSize(1);
        assertThat(propuesta.faltas().get(0).secciones()).isEqualTo(1);
        assertThat(propuesta.faltas().get(0).capacidadMinima()).isEqualTo(30);
        assertThat(propuesta.faltas().get(0).pabellon()).isEqualTo("Primaria");
    }

    @Test
    void elijeElAulaDeMenorCapacidadQueSirve() {
        Pabellon primaria = pabellon(2, "Primaria", 20L);
        Aula grande = aula(201, "P-GRANDE", 45, primaria);
        Aula justa = aula(202, "P-JUSTA", 32, primaria);
        Grado g1 = grado(20, "1° de Primaria");
        Seccion s = seccion(1, g1, "A", Turno.MANANA, 30, null);

        when(secciones.buscar(any(), any())).thenReturn(List.of(s));
        when(aulas.findAll()).thenReturn(List.of(grande, justa));
        when(pabellones.findAll()).thenReturn(List.of(primaria));

        var propuesta = servicio.calcular(7L);

        assertThat(propuesta.asignaciones().get(0).aula()).isEqualTo("P-JUSTA");
    }

    @Test
    void sinPabellonDelGradoNoAsignaYAvisa() {
        Grado g = grado(30, "Sin pabellón");
        Seccion s = seccion(1, g, "A", Turno.MANANA, 30, null);

        when(secciones.buscar(any(), any())).thenReturn(List.of(s));
        when(aulas.findAll()).thenReturn(List.of());
        when(pabellones.findAll()).thenReturn(List.of());

        var propuesta = servicio.calcular(7L);

        assertThat(propuesta.asignables()).isZero();
        assertThat(propuesta.faltas().get(0).pabellon()).isNull();
        assertThat(propuesta.faltas().get(0).motivo()).contains("no tiene pabellón asignado");
    }

    @Test
    void aplicarSoloGuardaLasAsignacionesPosibles() {
        Pabellon primaria = pabellon(2, "Primaria", 20L);
        Aula pri1 = aula(201, "P-1", 35, primaria);
        Grado g1 = grado(20, "1° de Primaria");
        Seccion ok = seccion(1, g1, "A", Turno.MANANA, 30, null);
        Seccion sinAula = seccion(2, g1, "B", Turno.MANANA, 30, null);

        when(secciones.buscar(any(), any())).thenReturn(List.of(ok, sinAula));
        when(aulas.findAll()).thenReturn(List.of(pri1));
        when(pabellones.findAll()).thenReturn(List.of(primaria));

        var resultado = servicio.aplicar(7L);

        assertThat(resultado.asignadas()).isEqualTo(1);
        assertThat(resultado.sinAula()).isEqualTo(1);
        assertThat(ok.getAula()).isSameAs(pri1);
        assertThat(sinAula.getAula()).isNull();
        verify(secciones).save(ok);
        verify(secciones, never()).save(sinAula);
    }

    @Test
    void agregarAulaCreaUnCodigoLibreYSubeLasAulasDeclaradas() {
        Pabellon primaria = pabellon(2, "Primaria", 20L);
        primaria.setCantidadAulas((short) 3);
        when(pabellones.findById(2L)).thenReturn(java.util.Optional.of(primaria));
        when(aulas.findByPabellonIdOrderByCodigo(2L)).thenReturn(List.of(aula(201, "PRIMARIA-01", 35, primaria)));
        when(aulas.existsByCodigo("PRIMARIA-02")).thenReturn(true);   // ya ocupado: debe buscar el siguiente
        when(aulas.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Aula creada = servicio.agregarAula(2L, (short) 30);

        assertThat(creada.getCodigo()).isEqualTo("PRIMARIA-03");
        assertThat(creada.getCapacidad()).isEqualTo((short) 30);
        assertThat(creada.getActivo()).isTrue();
        assertThat(creada.getPabellon()).isSameAs(primaria);
        assertThat(primaria.getCantidadAulas()).isEqualTo((short) 4);
        verify(pabellones).save(primaria);
    }

    @Test
    void ignoraAulasInactivasYSeccionesInactivas() {
        Pabellon primaria = pabellon(2, "Primaria", 20L);
        Aula inactiva = aula(201, "P-OFF", 35, primaria);
        inactiva.setActivo(false);
        Grado g1 = grado(20, "1° de Primaria");
        Seccion s = seccion(1, g1, "A", Turno.MANANA, 30, null);

        when(secciones.buscar(any(), any())).thenReturn(List.of(s));
        when(aulas.findAll()).thenReturn(List.of(inactiva));
        when(pabellones.findAll()).thenReturn(List.of(primaria));

        var propuesta = servicio.calcular(7L);

        assertThat(propuesta.asignables()).isZero();
        assertThat(propuesta.faltas().get(0).grado()).isEqualTo("1° de Primaria");
    }
}
