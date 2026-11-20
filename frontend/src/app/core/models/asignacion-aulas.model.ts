/** Una sección y el aula que se le asignaría (aulaId null cuando no hay aula disponible). */
export interface AsignacionAulaItem {
  seccionId: number;
  seccion: string;
  gradoId: number;
  grado: string;
  turno: string;
  capacidad: number;
  aulaId: number | null;
  aula: string | null;
  pabellonId: number | null;
  pabellon: string | null;
  /** Por qué no se puede asignar (null cuando sí hay aula). */
  motivo: string | null;
}

/** Secciones de un grado que no encuentran aula en su pabellón. */
export interface FaltaAula {
  gradoId: number;
  grado: string;
  pabellonId: number | null;
  pabellon: string | null;
  secciones: number;
  capacidadMinima: number;
  motivo: string;
}

export interface PropuestaAulas {
  anioId: number;
  asignaciones: AsignacionAulaItem[];
  faltas: FaltaAula[];
  asignables: number;
  sinAula: number;
}

export interface ResultadoAsignacionAulas {
  asignadas: number;
  sinAula: number;
}
