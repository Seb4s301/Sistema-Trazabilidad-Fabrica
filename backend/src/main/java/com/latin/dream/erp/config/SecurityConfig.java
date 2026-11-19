package com.latin.dream.erp.config;

import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.infrastructure.security.JwtAuthFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;

/**
 * Configuración de infraestructura: Spring Security + JWT.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig implements WebMvcConfigurer {

    private final JwtAuthFilter jwtAuthFilter;

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://127.0.0.1:4200,http://localhost:4201,http://127.0.0.1:4201}")
    private String[] corsAllowedOrigins;

    @Value("${api.prefix:/api/v1}")
    private String apiPrefix;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList(corsAllowedOrigins));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.POST, apiPrefix + "/auth/login").permitAll()
                .requestMatchers(apiPrefix + "/administracion/**").hasAnyAuthority("gerente", "supervisora")
                .requestMatchers(org.springframework.http.HttpMethod.POST, apiPrefix + "/comisiones/**").hasAnyAuthority("gerente", "supervisora", "contabilidad", "caja_chica")
                .requestMatchers(org.springframework.http.HttpMethod.POST, apiPrefix + "/despachos/**").hasAnyAuthority("gerente", "supervisora", "conductor")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, apiPrefix + "/despachos/**").hasAnyAuthority("gerente", "supervisora", "conductor")
                .requestMatchers(org.springframework.http.HttpMethod.PUT, apiPrefix + "/ordenes/**").hasAnyAuthority("gerente", "supervisora", "vendedora")
                .requestMatchers(org.springframework.http.HttpMethod.DELETE, apiPrefix + "/ordenes/**").hasAnyAuthority("gerente", "supervisora", "vendedora")
                .requestMatchers(apiPrefix + "/comisiones/**").hasAnyAuthority("gerente", "supervisora", "contabilidad", "caja_chica")
                .requestMatchers(org.springframework.http.HttpMethod.PATCH, apiPrefix + "/despachos/**").hasAnyAuthority("gerente", "supervisora", "conductor")
                .requestMatchers(org.springframework.http.HttpMethod.POST, apiPrefix + "/produccion/**").hasAnyAuthority("gerente", "supervisora", "encargado_area")
                .requestMatchers(org.springframework.http.HttpMethod.POST, apiPrefix + "/ordenes/**").hasAnyAuthority("gerente", "supervisora", "vendedora")
                .anyRequest().authenticated()
            )
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> response.sendError(401))
                .accessDeniedHandler((request, response, ex) -> response.sendError(403)))
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new HandlerMethodArgumentResolver() {

            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return parameter.hasParameterAnnotation(com.latin.dream.erp.crosscutting.annotation.UsuarioActual.class)
                    && Usuario.class.isAssignableFrom(parameter.getParameterType());
            }

            @Override
            public Object resolveArgument(MethodParameter parameter,
                                          ModelAndViewContainer mavContainer,
                                          NativeWebRequest webRequest,
                                          WebDataBinderFactory binderFactory) {
                var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof Usuario) {
                    return auth.getPrincipal();
                }
                return null;
            }
        });
    }
}
