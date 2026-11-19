// Adaptador de servicio para aulas segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class AulasService {
  protected readonly planificacionService = inject(PlanificacionService);
}
