package com.latin.dream.erp.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Orden de fabricación.
 * Número: OP-AAAA-NNNN (secuencia global).
 */
@Entity
@Table(name = "ordenes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Orden {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 20, unique = true)
    private String numero; // OP-AAAA-NNNN

    @Column(name = "solicitud_id", unique = true, length = 36)
    private String solicitudId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "modelo_id")
    private Modelo modelo;

    @Column
    private Integer cantidad;

    @Column(length = 20)
    private String prioridad; // Alta, Normal, Baja

    @Column(name = "fecha_entrega")
    private LocalDate fechaEntrega;

    @Column(length = 1000)
    private String observaciones;

    @Column(nullable = false, length = 30)
    private String estado; // borrador, pendiente_corte, en_proceso, calidad, listo_despacho, completado, cancelado

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registrado_por")
    private Usuario registradoPor;

    @Column(length = 20)
    private String turno;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    @PrePersist
    void onCreate() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
