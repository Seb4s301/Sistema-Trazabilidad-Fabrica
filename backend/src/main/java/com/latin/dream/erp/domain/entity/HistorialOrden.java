package com.latin.dream.erp.domain.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="historial_ordenes") @Getter @Setter @NoArgsConstructor
public class HistorialOrden {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="orden_id", nullable=false) private Orden orden;
    @Column(name="estado_anterior", length=30) private String estadoAnterior;
    @Column(name="estado_nuevo", nullable=false, length=30) private String estadoNuevo;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="usuario_id") private Usuario usuario;
    @Column(nullable=false) private LocalDateTime fecha;
    @Column(length=1000) private String notas;
}
