package com.latin.dream.erp.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * Entidad de dominio: Modelo de colchón del catálogo.
 */
@Entity
@Table(name = "modelos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Modelo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 20, unique = true)
    private String codigo;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 50)
    private String categoria; // 1.5 Plazas, 2 Plazas, Queen, King

    @Column(nullable = false, length = 100)
    private String dimensiones; // ej. "160 × 200 cm · Altura: 30 cm"

    @Column(nullable = false, length = 100)
    private String relleno; // Resortes Ensacados, Memory Foam, etc.

    @Column(nullable = false)
    private Boolean activo = true;
}
