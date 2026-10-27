package com.latin.dream.erp.domain.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

/**
 * Entidad de dominio: Usuario del sistema.
 * Roles: gerente, supervisora, vendedora, encargado_area, contabilidad, caja_chica, conductor.
 */
@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(nullable = false, length = 150, unique = true)
    private String correo;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String rol;

    @Column(length = 20)
    private String turno;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    public boolean esGerente() {
        return "gerente".equalsIgnoreCase(rol);
    }

    public boolean puedeVerComisiones() {
        return rol.equalsIgnoreCase("gerente")
            || rol.equalsIgnoreCase("supervisora")
            || rol.equalsIgnoreCase("contabilidad")
            || rol.equalsIgnoreCase("caja_chica");
    }

    public boolean puedeGestionarDespacho() {
        return rol.equalsIgnoreCase("gerente")
            || rol.equalsIgnoreCase("supervisora")
            || rol.equalsIgnoreCase("conductor");
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
