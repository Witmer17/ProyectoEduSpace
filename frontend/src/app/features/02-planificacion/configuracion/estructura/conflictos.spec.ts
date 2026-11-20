import { describe, expect, it } from 'vitest';
import { Grado, Seccion } from '../../../../core/models/planificacion.model';
import { detectarConflictos, sinRepetidos } from './conflictos';

const grado = (id: number, nombre: string) => ({ id, nombre, numero: id, activo: true, nivelId: 1 } as unknown as Grado);
const seccion = (id: number, g: Grado, nombre: string, aulaId: number) =>
  ({ id, gradoId: g.id, gradoNombre: g.nombre, nombre, turno: 'MANANA', capacidad: 30, aulaId, activo: true } as unknown as Seccion);

describe('detectarConflictos: avisos agrupados', () => {
  it('muestra una sola vez la falta de bloques y una sola la de malla, aunque afecten a varios grados', () => {
    const g1 = grado(1, '3 años');
    const g2 = grado(2, '4 años');
    const aulas = [{ id: 1, codigo: 'A-01', capacidad: 30, activo: true }, { id: 2, codigo: 'A-02', capacidad: 30, activo: true }] as never;
    const conflictos = sinRepetidos(detectarConflictos({
      grados: [g1, g2],
      secciones: [seccion(1, g1, 'A', 1), seccion(2, g2, 'A', 2)],
      aulas,
      cursos: [],
      bloques: [],
      mallas: { 1: { gradoId: 1, gradoNombre: '3 años', totalHoras: 0, cursos: [] }, 2: { gradoId: 2, gradoNombre: '4 años', totalHoras: 0, cursos: [] } },
    }));

    const malla = conflictos.filter(c => c.clave.startsWith('sin-malla'));
    const bloques = conflictos.filter(c => c.clave.startsWith('sin-bloques'));
    expect(malla).toHaveLength(1);
    expect(malla[0].texto).toContain('2 grados');
    expect(malla[0].ruta).toBe('/planificacion/malla');
    expect(bloques).toHaveLength(1);
    expect(bloques[0].texto).toContain('mañana');
    expect(bloques[0].ruta).toBe('/planificacion/bloques');
    expect(conflictos).toHaveLength(2);
  });
});
