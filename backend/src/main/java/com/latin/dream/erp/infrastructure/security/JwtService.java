package com.latin.dream.erp.infrastructure.security;

import com.latin.dream.erp.domain.entity.Usuario;
import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Date;
import java.util.function.Function;

/**
 * Servicio de infraestructura: generación y extracción de claims del JWT.
 */
@Service
public class JwtService {

    @Autowired
    private JwtConfig jwtConfig;

    public String extraerUsername(String token) {
        return extraerClaim(token, Claims::getSubject);
    }

    public Date extraerExpiracion(String token) {
        return extraerClaim(token, Claims::getExpiration);
    }

    public <T> T extraerClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extraerTodosLosClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extraerTodosLosClaims(String token) {
        // jjwt 0.12.x: Jwts.parser() devuelve JwtParserBuilder (parserBuilder() fue eliminado)
        return Jwts.parser()
            .verifyWith(jwtConfig.secretKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }

    private Boolean estaExpirado(String token) {
        return extraerExpiracion(token).before(new Date());
    }

    public String generarToken(Usuario usuario) {
        return Jwts.builder()
            .setSubject(usuario.getCorreo())
            .claim("rol", usuario.getRol())
            .claim("nombre", usuario.getNombre())
            .claim("turno", usuario.getTurno())
            .setIssuedAt(new Date(System.currentTimeMillis()))
            .setExpiration(new Date(System.currentTimeMillis() + jwtConfig.getExpiration()))
            .signWith(jwtConfig.secretKey())
            .compact();
    }

    public boolean esValido(String token, Usuario usuarioDetails) {
        final String username = extraerUsername(token);
        return (username.equals(usuarioDetails.getCorreo())) && !estaExpirado(token);
    }
}
