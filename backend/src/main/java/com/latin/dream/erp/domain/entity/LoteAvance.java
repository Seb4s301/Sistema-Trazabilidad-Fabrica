package com.latin.dream.erp.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Avance de lote (trazabilidad).
 * Cada vez que un lote cambia de área se registra aquí con responsable, inicio y fin.
 */
@Entity
@Table(name = "lote_avances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoteAvance {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    @Column(name = "area_origen", length = 30)
    private String areaOrigen;

    @Column(name = "area_destino", nullable = false, length = 30)
    private String areaDestino;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "responsable_id", nullable = false)
    private Usuario responsable;

    @Column(name = "inicio", nullable = false)
    private LocalDateTime inicio;

    @Column(name = "fin")
    private LocalDateTime fin;

    @Column(length = 20)
    private String resultado; // aprobado, rechazado (solo Calidad)

    @Column(length = 1000)
    private String notas;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @PrePersist
    void onCreate() {
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
    }
}
