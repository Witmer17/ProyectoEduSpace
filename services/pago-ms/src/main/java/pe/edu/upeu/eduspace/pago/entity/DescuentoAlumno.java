package pe.edu.upeu.eduspace.pago.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "descuentos_alumno", uniqueConstraints = {
    @UniqueConstraint(name = "uk_desc_alumno", columnNames = {"alumno_id", "descuento_id", "anio_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

//resolución o constancia de beca otorgada


public class DescuentoAlumno {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alumno_id", nullable = false)
    private Long alumnoId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "descuento_id", nullable = false)
    private Descuento descuento;

    @Column(name = "anio_id", nullable = false)
    private Long anioId;

    @Column(length = 300)
    private String motivo;

    @Column(name = "aprobado_por")
    private Long aprobadoPor;


    //si el estudiante baja su rendimiento se le quita la beca y si no se queda como tal

    @Column(nullable = false)
    @Builder.Default
    private Boolean vigente = true;
}

