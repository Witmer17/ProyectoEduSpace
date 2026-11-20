// Adaptador de servicio para secciones segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class SeccionesService {
  protected readonly planificacionService = inject(PlanificacionService);
}
