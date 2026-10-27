package com.latin.dream.erp.application.service;

import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.domain.repository.UsuarioRepository;
import com.latin.dream.erp.application.dto.AuthResponse;
import com.latin.dream.erp.infrastructure.security.JwtService;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación: autenticación de usuarios.
 */
@Service
@Transactional(readOnly = true)
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UsuarioRepository usuarioRepository,
                       JwtService jwtService,
                       PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse autenticar(String correo, String password) {
        Usuario usuario = usuarioRepository.findByCorreo(correo)
            .orElseThrow(() -> new BusinessException("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos"));

        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new BusinessException("CREDENCIALES_INVALIDAS", "Correo o contraseña incorrectos");
        }

        if (!usuario.getActivo()) {
            throw new BusinessException("USUARIO_INACTIVO", "El usuario está inactivo. Contacta a soporte TI");
        }

        String token = jwtService.generarToken(usuario);
        return new AuthResponse(token, usuario.getCorreo(), usuario.getRol(), usuario.getNombre());
    }
}
