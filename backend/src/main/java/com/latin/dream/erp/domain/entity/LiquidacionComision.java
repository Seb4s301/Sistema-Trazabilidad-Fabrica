package com.latin.dream.erp.domain.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
@Entity @Table(name="liquidaciones_comision") @Getter @Setter @NoArgsConstructor
public class LiquidacionComision {
    @Id @GeneratedValue(strategy=GenerationType.UUID) private String id;
    @OneToOne(fetch=FetchType.LAZY) @JoinColumn(name="orden_id",nullable=false,unique=true) private Orden orden;
    @Column(nullable=false,length=7) private String periodo;
    @Column(nullable=false,length=100) private String modelo;
    @Column(nullable=false,length=50) private String categoria;
    @Column(nullable=false,length=100) private String relleno;
    @Column(nullable=false) private Integer unidades;
    @Column(name="precio_base",nullable=false,precision=12,scale=2) private BigDecimal precioBase;
    @Column(nullable=false,precision=5,scale=2) private BigDecimal porcentaje;
    @Column(nullable=false,precision=14,scale=2) private BigDecimal monto;
    @Column(name="liquidado_en",nullable=false) private LocalDateTime liquidadoEn;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="liquidado_por",nullable=false) private Usuario liquidadoPor;
    @Column(name="pagado_en") private LocalDateTime pagadoEn;
    @ManyToOne(fetch=FetchType.LAZY) @JoinColumn(name="pagado_por") private Usuario pagadoPor;
    @Column(name="referencia_pago",length=200) private String referenciaPago;
}
