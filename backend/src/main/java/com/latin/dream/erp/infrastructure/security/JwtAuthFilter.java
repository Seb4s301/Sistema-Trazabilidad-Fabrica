package com.latin.dream.erp.infrastructure.security;

import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.domain.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Collections;

/**
 * Filtro de infraestructura: valida el JWT en cada petición protegida.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String correoUsuario;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        jwt = authHeader.substring(7);
        correoUsuario = extraerCorreoSeguro(jwt);
        if (correoUsuario == null) {
            // Token vencido, malformado o firma inválida → no autenticar (el SecurityFilterChain responde 401/403)
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            Usuario usuario = usuarioRepository.findByCorreo(correoUsuario).orElse(null);
            if (usuario != null && jwtService.esValido(jwt, usuario)) {
                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(usuario, null, Collections.emptyList());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        filterChain.doFilter(request, response);
    }

    /**
     * Extrae el correo del token; devuelve null si el token es inválido
     * (vencido, malformado o con firma incorrecta) para no romper la petición.
     */
    private String extraerCorreoSeguro(String token) {
        try {
            return jwtService.extraerUsername(token);
        } catch (Exception e) {
            return null;
        }
    }
}
