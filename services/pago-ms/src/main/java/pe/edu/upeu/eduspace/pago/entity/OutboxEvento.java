package pe.edu.upeu.eduspace.pago.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "outbox_eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OutboxEvento {

    @Id
    private UUID id;
    
    
    //aviso de que trata como por ejemplo cuota, pago, comprobante 

    @Column(name = "aggregate_type", nullable = false, length = 40)
    private String aggregateType;

    @Column(name = "aggregate_id", nullable = false, length = 40)
    private String aggregateId;

    //destino del mensaje
    @Column(nullable = false, length = 80)
    private String topic;

        //oden de mensajes

    @Column(nullable = false, length = 80)
    private String clave;
 

    //informacion detallada
    @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    @Column(nullable = false, columnDefinition = "jsonb")
    private String payload;

    @Column(name = "creado_en", nullable = false)
    @Builder.Default
    private LocalDateTime creadoEn = LocalDateTime.now();

    @Column(name = "enviado_en")
    private LocalDateTime enviadoEn;

    @Column(nullable = false)
    @Builder.Default
    private Short intentos = 0;


    //ayuda al equipo a diagnosticar el error
    @Column(name = "ultimo_error", length = 500)
    private String ultimoError;
}

