package com.latin.dream.erp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Propiedades de la semilla de usuarios de prueba.
 * Los datos se leen de application.yml (no se hardcodean en el código).
 *
 * app.seed.enabled=false en producción.
 */
@Data
@Component
@ConfigurationProperties(prefix = "app.seed")
public class SeedProperties {

    /** Activa la creación de usuarios de prueba al arrancar (solo desarrollo). */
    private boolean enabled = false;

    /** Contraseña compartida de los usuarios semilla (se guarda con BCrypt). */
    private String password = "";

    /** Usuarios a crear si la tabla está vacía. */
    private List<UsuarioSemilla> usuarios = new ArrayList<>();

    @Data
    public static class UsuarioSemilla {
        private String nombre;
        private String correo;
        private String rol;
        private String turno;
    }
}
