package com.latin.dream.erp.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Lote (uno por orden).
 * Código: LT-AAMM-NNNN.
 * Se mueve de área en área; cada movimiento crea un LoteAvance.
 */
@Entity
@Table(name = "lotes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 20, unique = true)
    private String codigo; // LT-AAMM-NNNN

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_id", nullable = false)
    private Orden orden;

    @Column(nullable = false)
    private Integer unidades;

    @Column(nullable = false, length = 30)
    private String areaActual; // Corte, Ensamblaje, Tapizado, Calidad, Listo

    @Column(name = "fecha_entrega", nullable = false)
    private LocalDate fechaEntrega;

    @Column(nullable = false)
    private Boolean retrasado = false;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public boolean estaEnPlanta() {
        return !"Listo".equalsIgnoreCase(areaActual);
    }

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
