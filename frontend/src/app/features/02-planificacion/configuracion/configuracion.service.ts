// Adaptador de servicio para configuracion segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class ConfiguracionService {
  protected readonly planificacionService = inject(PlanificacionService);
}
