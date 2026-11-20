// Adaptador de servicio para cursos segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class CursosService {
  protected readonly planificacionService = inject(PlanificacionService);
}
