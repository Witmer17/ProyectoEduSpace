// Adaptador de servicio para apertura segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class AperturaService {
  protected readonly planificacionService = inject(PlanificacionService);
}
