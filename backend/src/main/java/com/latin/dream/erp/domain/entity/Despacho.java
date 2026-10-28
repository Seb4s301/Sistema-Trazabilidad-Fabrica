package com.latin.dream.erp.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Despacho y Transporte.
 */
@Entity
@Table(name = "despachos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Despacho {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 20, unique = true)
    private String codigo; // DS-XXXX

    @Column(nullable = false, length = 150)
    private String cliente;

    @Column(nullable = false, length = 200)
    private String destino;

    @Column(nullable = false)
    private Integer unidades;

    @Column(nullable = false, length = 30)
    private String estado; // programado, transito, entregado, cancelado

    @Column(name = "fecha_salida")
    private LocalDate fechaSalida;

    @Column(name = "fecha_entrega_estimada")
    private LocalDate fechaEntregaEstimada;

    @Column(length = 50)
    private String vehiculo;

    @Column(length = 100)
    private String conductor;

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
