import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class AniosService {
  private readonly planificacionService = inject(PlanificacionService);

  anios = () => this.planificacionService.anios();
  anioActivo = () => this.planificacionService.anioActivo();
  crearAnio = (body: any) => this.planificacionService.crearAnio(body);
  actualizarAnio = (id: number, body: any) => this.planificacionService.actualizarAnio(id, body);
  activarAnio = (id: number) => this.planificacionService.activarAnio(id);
  cerrarAnio = (id: number) => this.planificacionService.cerrarAnio(id);
  periodos = (anioId: number) => this.planificacionService.periodos(anioId);
  crearPeriodo = (body: any) => this.planificacionService.crearPeriodo(body);
  actualizarPeriodo = (id: number, body: any) => this.planificacionService.actualizarPeriodo(id, body);
  eliminarPeriodo = (id: number) => this.planificacionService.eliminarPeriodo(id);
}
