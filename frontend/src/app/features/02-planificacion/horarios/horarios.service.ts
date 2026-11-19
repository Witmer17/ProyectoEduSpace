// Adaptador de servicio para horarios segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class HorariosService {
  protected readonly planificacionService = inject(PlanificacionService);
}
