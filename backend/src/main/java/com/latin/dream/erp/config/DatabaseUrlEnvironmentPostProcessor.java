package com.latin.dream.erp.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Normaliza variables de entorno de conexión a PostgreSQL para que el backend
 * acepte tanto el formato JDBC (jdbc:postgresql://...) como el formato de
 * "connection string" que entregan Supabase/Heroku/Render (postgresql://... o
 * postgres://...), evitando errores al desplegar.
 */
public class DatabaseUrlEnvironmentPostProcessor implements EnvironmentPostProcessor {

    private static final String PROPERTY_SOURCE_NAME = "databaseUrlNormalizer";

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String rawUrl = firstNonBlank(environment.getProperty("DB_URL"), environment.getProperty("DATABASE_URL"));
        if (rawUrl == null || rawUrl.isBlank() || rawUrl.trim().startsWith("jdbc:")) {
            return;
        }

        String trimmed = rawUrl.trim();
        if (!trimmed.startsWith("postgres://") && !trimmed.startsWith("postgresql://")) {
            return;
        }

        try {
            URI uri = new URI(trimmed);
            String host = uri.getHost();
            if (host == null || host.isBlank()) {
                return;
            }
            int port = uri.getPort() == -1 ? 5432 : uri.getPort();
            String path = uri.getPath();
            if (path == null || path.isBlank() || "/".equals(path)) {
                path = "/postgres";
            }
            String query = uri.getQuery();
            boolean hasSslMode = query != null && query.contains("sslmode=");
            if (!hasSslMode && (host.endsWith("supabase.co") || host.endsWith("supabase.com"))) {
                query = (query == null || query.isBlank()) ? "sslmode=require" : query + "&sslmode=require";
            }

            String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path;
            if (query != null && !query.isBlank()) {
                jdbcUrl = jdbcUrl + "?" + query;
            }

            Map<String, Object> properties = new HashMap<>();
            properties.put("DB_URL", jdbcUrl);

            String userInfo = uri.getUserInfo();
            if (userInfo != null && !userInfo.isBlank()) {
                String[] parts = userInfo.split(":", 2);
                String username = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                if (!username.isBlank()) {
                    properties.put("DB_USER", username);
                }
                if (parts.length == 2) {
                    String password = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                    if (!password.isBlank()) {
                        properties.put("DB_PASSWORD", password);
                    }
                }
            }

            environment.getPropertySources().addFirst(new MapPropertySource(PROPERTY_SOURCE_NAME, properties));
        } catch (Exception ignored) {
            // Si no se puede parsear, se deja el valor original para que Spring reporte el error real.
        }
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return null;
    }
}
