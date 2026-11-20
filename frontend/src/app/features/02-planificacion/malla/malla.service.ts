// Adaptador de servicio para malla segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class MallaService {
  protected readonly planificacionService = inject(PlanificacionService);
}
