import { Routes } from '@angular/router';
import { authGuard, guestGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';
import { ShellComponent } from './layout/shell/shell.component';
import { AuthService } from './core/services/auth.service';
import { rutaInicial } from './core/utils/landing';
import { inject } from '@angular/core';

const PERSONAL = ['ADMIN', 'SECRETARIA'];
const DOCENTE = ['DOCENTE'];
const FAMILIA = ['APODERADO', 'ALUMNO'];
const SOLO_ADMIN = ['ADMIN'];

export const routes: Routes = [
  // Sitio público del colegio: no exige sesión.
  {
    path: '',
    loadComponent: () => import('./features/11-portal-publico/publico-layout.component').then(m => m.PublicoLayoutComponent),
    children: [
      { path: '', pathMatch: 'full', title: 'Colegio Horizonte del Valle', loadComponent: () => import('./features/11-portal-publico/inicio/inicio-publico.component').then(m => m.InicioPublicoComponent) },
      { path: 'nosotros', title: 'Nosotros · Horizonte del Valle', loadComponent: () => import('./features/11-portal-publico/nosotros/nosotros.component').then(m => m.NosotrosComponent) },
      { path: 'niveles', title: 'Niveles · Horizonte del Valle', loadComponent: () => import('./features/11-portal-publico/niveles/niveles-publico.component').then(m => m.NivelesPublicoComponent) },
      { path: 'admision', title: 'Admisión · Horizonte del Valle', loadComponent: () => import('./features/11-portal-publico/admision/admision.component').then(m => m.AdmisionComponent) },
      { path: 'matricula-en-linea', title: 'Matrícula en línea · Horizonte del Valle', loadComponent: () => import('./features/11-portal-publico/matricula-en-linea/matricula-en-linea.component').then(m => m.MatriculaEnLineaComponent) },
      { path: 'noticias', title: 'Noticias · Horizonte del Valle', loadComponent: () => import('./features/11-portal-publico/noticias/noticias.component').then(m => m.NoticiasComponent) },
      { path: 'contacto', title: 'Contacto · Horizonte del Valle', loadComponent: () => import('./features/11-portal-publico/contacto/contacto.component').then(m => m.ContactoComponent) }
    ]
  },
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/01-auth/login/login.component').then(m => m.LoginComponent)
  },
  {
    path: 'cambiar-clave',
    canActivate: [authGuard],
    loadComponent: () => import('./features/01-auth/cambiar-clave/cambiar-clave.component').then(m => m.CambiarClaveComponent)
  },
  {
    path: '',
    component: ShellComponent,
    canActivate: [authGuard],
    children: [
      // Cada rol arranca en su propia pantalla de inicio.
      { path: 'portal', pathMatch: 'full', redirectTo: () => rutaInicial(inject(AuthService).roles()) },
      {
        path: 'calendario',
        loadComponent: () => import('./features/12-comun/calendario/calendario.component').then(m => m.CalendarioComponent)
      },
      {
        path: 'ayuda',
        loadComponent: () => import('./features/12-comun/ayuda/ayuda.component').then(m => m.AyudaComponent)
      },
      // Lo propio de quien trabaja en el colegio: su contrato, asistencia, pagos y cuenta de depósito (solo lectura).
      {
        path: 'mi-trabajo',
        canActivate: [roleGuard],
        data: { roles: ['DOCENTE', 'CAJERO'] },
        loadComponent: () => import('./features/12-comun/mi-trabajo/mi-trabajo.component').then(m => m.MiTrabajoComponent)
      },
      {
        path: 'mi-perfil',
        loadComponent: () => import('./features/01-auth/perfil/perfil.component').then(m => m.PerfilComponent)
      },
      {
        path: 'finanzas',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/08-caja-pagos/finanzas-dashboard/finanzas.component').then(m => m.FinanzasComponent)
      },
      {
        path: 'docente/alumnos',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/12-comun/dashboard/docente/alumnos/docente-alumnos.component').then(m => m.DocenteAlumnosComponent)
      },
      {
        path: 'familia/boletines',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/10-calificaciones/familia-notas/boletines/familia-boletines.component').then(m => m.FamiliaBoletinesComponent)
      },
      {
        path: 'notificaciones',
        loadComponent: () => import('./features/07-notificaciones/bandeja/notificaciones.component').then(m => m.NotificacionesComponent)
      },
      {
        path: 'notificaciones/envio',
        canActivate: [roleGuard],
        data: { roles: SOLO_ADMIN },
        loadComponent: () => import('./features/07-notificaciones/envio-admin/notificaciones-admin.component').then(m => m.NotificacionesAdminComponent)
      },
      {
        path: 'inicio',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/12-comun/dashboard/dashboard.component').then(m => m.DashboardComponent)
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard],
        data: { roles: SOLO_ADMIN },
        loadComponent: () => import('./features/01-auth/usuarios/usuarios.component').then(m => m.UsuariosComponent)
      },
      {
        path: 'cuentas',
        canActivate: [roleGuard],
        data: { roles: SOLO_ADMIN },
        loadComponent: () => import('./features/08-caja-pagos/cuentas-alumnos/cuentas.component').then(m => m.CuentasComponent)
      },
      {
        path: 'alumnos',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/03-alumnos/lista-alumnos/alumnos.component').then(m => m.AlumnosComponent)
      },
      {
        path: 'alumnos/importar',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/03-alumnos/importar-alumnos/importar.component').then(m => m.ImportarAlumnosComponent)
      },
      {
        path: 'asistencia/justificaciones',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/05-asistencia/justificaciones/justificaciones.component').then(m => m.JustificacionesComponent)
      },
      {
        path: 'asistencia/reporte',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/05-asistencia/reporte-asistencia/reporte.component').then(m => m.ReporteAsistenciaComponent)
      },
      {
        path: 'calificaciones/cierre',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/10-calificaciones/cierre-periodo/cierre.component').then(m => m.CierrePeriodoComponent)
      },
      {
        path: 'calificaciones/boletines',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/10-calificaciones/boletines/boletines.component').then(m => m.BoletinesComponent)
      },
      {
        path: 'pagos/cuentas',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/08-caja-pagos/cuentas-alumnos/cuentas.component').then(m => m.CuentasComponent)
      },
      {
        path: 'pagos/cobranza',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/08-caja-pagos/cobranza-morosos/cobranza.component').then(m => m.CobranzaComponent)
      },
      {
        path: 'alumnos/:id',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/03-alumnos/alumno-detalle/alumno-detalle.component').then(m => m.AlumnoDetalleComponent)
      },
      {
        path: 'matriculas',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/04-matriculas/matriculas/matriculas.component').then(m => m.MatriculasComponent)
      },
      {
        path: 'matriculas/aprobaciones',
        canActivate: [roleGuard],
        data: { roles: SOLO_ADMIN },
        loadComponent: () => import('./features/04-matriculas/aprobaciones/aprobaciones.component').then(m => m.AprobacionesComponent)
      },
      {
        path: 'matriculas/campanias',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/04-matriculas/campanias/campanias.component').then(m => m.CampaniasComponent)
      },
      {
        path: 'matriculas/cupos',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/04-matriculas/cupos/cupos.component').then(m => m.CuposComponent)
      },
      {
        path: 'docente/inicio',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/12-comun/dashboard/docente/inicio/docente-inicio.component').then(m => m.DocenteInicioComponent)
      },
      {
        path: 'docente/curso/:seccionId/:cursoId',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/12-comun/dashboard/docente/curso/docente-curso.component').then(m => m.DocenteCursoComponent)
      },
      {
        path: 'docente/horario',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/12-comun/dashboard/docente/horario/docente-horario.component').then(m => m.DocenteHorarioComponent)
      },
      {
        path: 'docente/asistencia',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/05-asistencia/docente-asistencia/docente-asistencia.component').then(m => m.DocenteAsistenciaComponent)
      },
      {
        path: 'docente/tareas',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/06-tareas/docente-tareas/docente-tareas.component').then(m => m.DocenteTareasComponent)
      },
      {
        path: 'docente/tareas/:id',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/06-tareas/tarea-entregas/tarea-entregas.component').then(m => m.TareaEntregasComponent)
      },
      {
        path: 'docente/examenes',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/09-examenes/docente-examenes/docente-examenes.component').then(m => m.DocenteExamenesComponent)
      },
      {
        path: 'docente/banco',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/09-examenes/banco-preguntas/docente-banco.component').then(m => m.DocenteBancoComponent)
      },
      {
        path: 'docente/notas',
        canActivate: [roleGuard],
        data: { roles: DOCENTE },
        loadComponent: () => import('./features/10-calificaciones/docente-notas/docente-notas.component').then(m => m.DocenteNotasComponent)
      },
      {
        path: 'familia/inicio',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/12-comun/dashboard/familia/cursos/familia-cursos.component').then(m => m.FamiliaCursosComponent)
      },
      {
        path: 'familia/curso/:cursoId',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/12-comun/dashboard/familia/cursos/familia-curso.component').then(m => m.FamiliaCursoComponent)
      },
      {
        path: 'familia/asistencia',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/05-asistencia/familia-asistencia/familia-asistencia.component').then(m => m.FamiliaAsistenciaComponent)
      },
      {
        path: 'familia/notas',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/10-calificaciones/familia-notas/notas/familia-notas.component').then(m => m.FamiliaNotasComponent)
      },
      {
        path: 'familia/horario',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/12-comun/dashboard/familia/horario/familia-horario.component').then(m => m.FamiliaHorarioComponent)
      },
      {
        path: 'familia/tareas',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/06-tareas/familia-tareas/familia-tareas.component').then(m => m.FamiliaTareasComponent)
      },
      {
        path: 'familia/examenes',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/09-examenes/familia-examenes/familia-examenes.component').then(m => m.FamiliaExamenesComponent)
      },
      {
        path: 'familia/examenes/:id/rendir',
        canActivate: [roleGuard],
        data: { roles: FAMILIA },
        loadComponent: () => import('./features/09-examenes/rendir-examen/rendir-examen.component').then(m => m.RendirExamenComponent)
      },
      {
        path: 'familia/pagos',
        canActivate: [roleGuard],
        data: { roles: ['APODERADO'] },
        loadComponent: () => import('./features/08-caja-pagos/familia-pagos/familia-pagos.component').then(m => m.FamiliaPagosComponent)
      },
      {
        path: 'caja',
        canActivate: [roleGuard],
        data: { roles: ['CAJERO'] },
        loadComponent: () => import('./features/08-caja-pagos/ventanilla-caja/caja.component').then(m => m.CajaComponent)
      },
      {
        path: 'planificacion/panel',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/aulas/panel/panel-aulas.component').then(m => m.PanelAulasComponent)
      },
      {
        path: 'planificacion/cursos-docentes',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/aulas/panel/panel-cursos.component').then(m => m.PanelCursosComponent)
      },
      {
        path: 'planificacion/aulas-disponibles',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/aulas/aulas-disponibles/aulas-disponibles.component').then(m => m.AulasDisponiblesComponent)
      },
      {
        path: 'configuracion',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/configuracion/resumen/configuracion-resumen.component').then(m => m.ConfiguracionResumenComponent)
      },
      {
        path: 'configuracion/colegio',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/configuracion/colegio/colegio-datos.component').then(m => m.ColegioDatosComponent)
      },
      {
        path: 'configuracion/grados',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/configuracion/grados/grados-oferta.component').then(m => m.GradosOfertaComponent)
      },
      {
        path: 'apertura',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/apertura/apertura.component').then(m => m.AperturaComponent)
      },
      {
        path: 'configuracion/cursos',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/configuracion/cursos/cursos-oferta.component').then(m => m.CursosOfertaComponent)
      },
      {
        path: 'configuracion/areas',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/configuracion/areas/areas-config.component').then(m => m.AreasConfigComponent)
      },
      {
        path: 'configuracion/estructura',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/configuracion/estructura/estructura.component').then(m => m.EstructuraComponent)
      },
      {
        path: 'configuracion/estructura/:gradoId',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/configuracion/estructura/grado-detalle.component').then(m => m.GradoDetalleComponent)
      },
      {
        path: 'planificacion/malla',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/malla/malla.component').then(m => m.MallaComponent)
      },
      {
        path: 'planificacion/asignaciones',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/horarios/asignaciones/asignaciones.component').then(m => m.AsignacionesComponent)
      },
      {
        path: 'planificacion/bloques',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/horarios/bloques/bloques.component').then(m => m.BloquesComponent)
      },
      {
        path: 'planificacion/horarios-auto',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/horarios/horario-auto.component').then(m => m.HorarioAutoComponent)
      },
      {
        path: 'planificacion/horarios',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/horarios/horarios.component').then(m => m.HorariosComponent)
      },
      {
        path: 'planificacion/feriados',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/feriados/feriados.component').then(m => m.FeriadosComponent)
      },
      {
        path: 'planificacion/anios',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/anios/anios.component').then(m => m.AniosComponent)
      },
      {
        path: 'planificacion/secciones',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/secciones/secciones.component').then(m => m.SeccionesComponent)
      },
      {
        path: 'planificacion/cursos',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/cursos/cursos.component').then(m => m.CursosComponent)
      },
      // Dentro de Personal: la asistencia es de ADMIN y SECRETARIA; el pago (contratos, sueldos, planilla) solo del ADMIN (el backend lo exige).
      // Páginas de las áreas de trabajo: el panel con todas y la página de cada una (las que tienen sección propia llevan allí desde su tarjeta).
      {
        path: 'areas',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/12-comun/secretaria/areas/areas-panel.component').then(m => m.AreasPanelComponent)
      },
      {
        path: 'areas/:codigo',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/12-comun/secretaria/areas/area-pagina.component').then(m => m.AreaPaginaComponent)
      },
      {
        path: 'personal/asistencia',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/05-asistencia/personal-asistencia/asistencia-personal.component').then(m => m.AsistenciaPersonalComponent)
      },
      {
        path: 'personal/pagos',
        canActivate: [roleGuard],
        data: { roles: ['ADMIN'] },
        loadComponent: () => import('./features/12-comun/personal/pago-personal.component').then(m => m.PagoPersonalComponent)
      },
      {
        path: 'personal',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/12-comun/personal/personal.component').then(m => m.PersonalComponent)
      },
      {
        path: 'planificacion/aulas',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/02-planificacion/aulas/aulas.component').then(m => m.AulasComponent)
      },
      {
        path: 'pagos/conceptos',
        canActivate: [roleGuard],
        data: { roles: SOLO_ADMIN },
        loadComponent: () => import('./features/08-caja-pagos/conceptos/conceptos.component').then(m => m.ConceptosComponent)
      },
      {
        path: 'pagos/tarifas',
        canActivate: [roleGuard],
        data: { roles: SOLO_ADMIN },
        loadComponent: () => import('./features/08-caja-pagos/tarifas/tarifas.component').then(m => m.TarifasComponent)
      },
      {
        path: 'pagos/descuentos',
        canActivate: [roleGuard],
        data: { roles: SOLO_ADMIN },
        loadComponent: () => import('./features/08-caja-pagos/descuentos/descuentos.component').then(m => m.DescuentosComponent)
      },
      {
        path: 'pagos/morosos',
        canActivate: [roleGuard],
        data: { roles: PERSONAL },
        loadComponent: () => import('./features/08-caja-pagos/cobranza-morosos/morosos.component').then(m => m.MorososComponent)
      }
    ]
  },
  { path: '**', redirectTo: '' }
];
