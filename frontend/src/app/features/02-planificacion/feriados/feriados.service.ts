// Adaptador de servicio para feriados segun MAPA-VISTAS-FRONTEND.md
import { inject, Injectable } from '@angular/core';
import { PlanificacionService } from '../../../core/services/planificacion.service';

@Injectable({ providedIn: 'root' })
export class FeriadosService {
  protected readonly planificacionService = inject(PlanificacionService);
}
