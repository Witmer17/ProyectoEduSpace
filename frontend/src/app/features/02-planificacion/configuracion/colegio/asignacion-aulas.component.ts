import { Component, OnInit, inject, signal } from '@angular/core';
import { FaltaAula, PropuestaAulas } from '../../../../core/models/asignacion-aulas.model';
import { Anio } from '../../../../core/models/planificacion.model';
import { AuthService } from '../../../../core/services/auth.service';
import { ConfirmService } from '../../../../core/services/confirm.service';
import { NotificationService } from '../../../../core/services/notification.service';
import { PlanificacionService } from '../../../../core/services/planificacion.service';
import { mensajeError } from '../../../../core/utils/errores';

/**
 * Asignación automática de aulas a las secciones del año activo. Solo llena las secciones sin aula (las que ya tienen no cambian);
 * cada grado usa el pabellón que lo declara. Si falta aula, muestra la advertencia con el botón para agregarla al pabellón del grado.
 */
@Component({
  selector: 'app-asignacion-aulas',
  template: `
    <section class="cfg-bloque" aria-labelledby="t-asig">
      <div class="asig-cab">
        <div>
          <h2 id="t-asig" class="sec-titulo">Asignación automática de aulas</h2>
          <p class="muted">Asigna un aula a cada sección que todavía no tiene. Cada grado usa el pabellón que lo declara. Las secciones que ya tienen aula no se tocan.</p>
        </div>
        <div class="asig-acciones">
          <button type="button" class="btn" [disabled]="cargando() || !anio()" (click)="calcular()">Calcular propuesta</button>
          @if (puedeEditar()) {
            @if (propuesta(); as p) {
              @if (p.asignables > 0) {
                <button type="button" class="btn btn-primary" [disabled]="guardando()" (click)="aplicar()">
                  Aplicar {{ p.asignables }} {{ p.asignables === 1 ? 'asignación' : 'asignaciones' }}
                </button>
              }
            }
          }
        </div>
      </div>

      @if (!anio()) {
        <div class="aviso aviso-warn">No hay un año académico activo: abre un periodo para asignar aulas.</div>
      }

      @if (propuesta(); as p) {
        <p class="resumen">
          <strong>{{ p.asignables }}</strong> {{ p.asignables === 1 ? 'sección recibiría aula' : 'secciones recibirían aula' }}
          · <strong>{{ p.sinAula }}</strong> sin aula
        </p>

        @for (f of p.faltas; track f.gradoId + ':' + f.pabellonId) {
          <div class="aviso aviso-warn falta" role="status">
            <div>
              <strong>{{ f.grado }}</strong>: faltan aulas para {{ f.secciones }} {{ f.secciones === 1 ? 'sección' : 'secciones' }}.
              <span class="muted">{{ f.motivo }}</span>
            </div>
            @if (puedeEditar() && f.pabellonId !== null) {
              <button type="button" class="btn btn-sm btn-primary" [disabled]="agregando() === f.gradoId" (click)="agregarAula(f)">
                Agregar aula (cap. {{ f.capacidadMinima }}) en {{ f.pabellon }}
              </button>
            }
          </div>
        }

        @if (p.asignaciones.length) {
          <div class="tabla-asig">
            <table>
              <thead>
                <tr><th scope="col">Sección</th><th scope="col">Grado</th><th scope="col">Turno</th><th scope="col">Vacantes</th><th scope="col">Aula propuesta</th><th scope="col">Pabellón</th></tr>
              </thead>
              <tbody>
                @for (a of p.asignaciones; track a.seccionId) {
                  <tr [class.sin-aula]="a.aulaId === null">
                    <td>{{ a.seccion }}</td>
                    <td>{{ a.grado }}</td>
                    <td>{{ a.turno === 'MANANA' ? 'Mañana' : 'Tarde' }}</td>
                    <td>{{ a.capacidad }}</td>
                    <td>@if (a.aula) { {{ a.aula }} } @else { <span class="error">Sin aula: {{ a.motivo }}</span> }</td>
                    <td>{{ a.pabellon ?? '—' }}</td>
                  </tr>
                }
              </tbody>
            </table>
          </div>
        }
      }

      @if (error()) { <div class="aviso aviso-danger" role="alert">{{ error() }}</div> }
    </section>
  `,
  styles: [`
    .asig-cab { display: flex; flex-wrap: wrap; gap: 12px; justify-content: space-between; align-items: flex-start; }
    .asig-acciones { display: flex; flex-wrap: wrap; gap: 8px; }
    .resumen { margin: 12px 0; }
    .falta { display: flex; flex-wrap: wrap; gap: 12px; justify-content: space-between; align-items: center; margin-top: 10px; }
    .tabla-asig { overflow-x: auto; margin-top: 14px; }
    .tabla-asig table { width: 100%; border-collapse: collapse; font-size: .9rem; }
    .tabla-asig th, .tabla-asig td { text-align: left; padding: 6px 8px; border-bottom: 1px solid var(--line); }
    .tabla-asig tr.sin-aula td { background: rgb(254 226 226 / .4); }
  `],
  imports: []
})
export class AsignacionAulasComponent implements OnInit {
  private readonly api = inject(PlanificacionService);
  private readonly avisos = inject(NotificationService);
  private readonly confirmacion = inject(ConfirmService);

  protected readonly puedeEditar = inject(AuthService).isAdmin;
  protected readonly anio = signal<Anio | null>(null);
  protected readonly propuesta = signal<PropuestaAulas | null>(null);
  protected readonly cargando = signal(false);
  protected readonly guardando = signal(false);
  protected readonly agregando = signal<number | null>(null);
  protected readonly error = signal<string | null>(null);

  ngOnInit(): void {
    this.api.anioActivo().subscribe({
      next: a => { this.anio.set(a); this.calcular(); },
      error: () => this.anio.set(null)
    });
  }

  protected calcular(): void {
    const a = this.anio();
    if (!a) return;
    this.cargando.set(true);
    this.error.set(null);
    this.api.asignacionAulas(a.id).subscribe({
      next: p => { this.propuesta.set(p); this.cargando.set(false); },
      error: e => { this.cargando.set(false); this.error.set(mensajeError(e, 'No se pudo calcular la asignación de aulas.')); }
    });
  }

  protected async aplicar(): Promise<void> {
    const a = this.anio();
    const p = this.propuesta();
    if (!a || !p || p.asignables === 0) return;
    const texto = p.asignables === 1 ? 'la sección' : `las ${p.asignables} secciones`;
    if (!(await this.confirmacion.confirmar(`¿Asignar aula a ${texto}? Las que ya tienen aula no cambian.`, 'Asignar', false))) return;
    this.guardando.set(true);
    this.api.aplicarAsignacionAulas(a.id).subscribe({
      next: r => {
        this.guardando.set(false);
        const sin = r.sinAula ? ` · ${r.sinAula} sin aula` : '';
        this.avisos.success(`${r.asignadas} ${r.asignadas === 1 ? 'sección asignada' : 'secciones asignadas'}${sin}.`);
        this.calcular();
      },
      error: e => { this.guardando.set(false); this.avisos.error(mensajeError(e, 'No se pudieron asignar las aulas.')); }
    });
  }

  protected agregarAula(f: FaltaAula): void {
    if (f.pabellonId === null) return;
    this.agregando.set(f.gradoId);
    this.api.agregarAulaPabellon(f.pabellonId, f.capacidadMinima).subscribe({
      next: aula => {
        this.agregando.set(null);
        this.avisos.success(`Aula ${aula.codigo} agregada en ${f.pabellon} (cap. ${aula.capacidad}).`);
        this.calcular();
      },
      error: e => { this.agregando.set(null); this.avisos.error(mensajeError(e, 'No se pudo agregar el aula.')); }
    });
  }
}
