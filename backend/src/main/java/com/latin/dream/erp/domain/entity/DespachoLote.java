package com.latin.dream.erp.domain.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity @Table(name="despacho_lotes") @Getter @Setter @NoArgsConstructor
public class DespachoLote {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="despacho_id",nullable=false) private Despacho despacho;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="lote_id",nullable=false) private Lote lote;
    @Column(nullable=false) private Integer cantidad;
    @Column(name="creado_en",nullable=false) private LocalDateTime creadoEn;
    @PrePersist void crear() { creadoEn=LocalDateTime.now(); }
}
