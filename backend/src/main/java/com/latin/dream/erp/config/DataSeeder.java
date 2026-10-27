package com.latin.dream.erp.config;

import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Siembra usuarios de prueba al arrancar, solo cuando:
 *  - app.seed.enabled = true, y
 *  - la tabla usuarios está vacía (idempotente).
 *
 * La contraseña se encripta con BCrypt en tiempo de ejecución;
 * los datos de los usuarios vienen de application.yml.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final SeedProperties seedProperties;

    public DataSeeder(UsuarioRepository usuarioRepository,
                      PasswordEncoder passwordEncoder,
                      SeedProperties seedProperties) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.seedProperties = seedProperties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!seedProperties.isEnabled()) {
            log.info("Seed de usuarios deshabilitado (app.seed.enabled=false).");
            return;
        }

        if (usuarioRepository.count() > 0) {
            log.info("La tabla usuarios ya tiene datos; seed omitido.");
            return;
        }

        String passwordEncriptada = passwordEncoder.encode(seedProperties.getPassword());

        for (SeedProperties.UsuarioSemilla semilla : seedProperties.getUsuarios()) {
            Usuario usuario = Usuario.builder()
                .nombre(semilla.getNombre())
                .correo(semilla.getCorreo())
                .password(passwordEncriptada)
                .rol(semilla.getRol())
                .turno(semilla.getTurno())
                .activo(true)
                .build();
            usuarioRepository.save(usuario);
            log.info("Usuario semilla creado: {} ({})", semilla.getCorreo(), semilla.getRol());
        }

        log.info("Seed completado: {} usuarios creados. Contraseña: {}",
            seedProperties.getUsuarios().size(), seedProperties.getPassword());
    }
}
