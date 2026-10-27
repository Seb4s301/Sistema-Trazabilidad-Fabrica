package com.latin.dream.erp.application.dto;

/**
 * DTO de salida de autenticación.
 */
public class AuthResponse {

    private String token;
    private String tipo = "Bearer";
    private String correo;
    private String rol;
    private String nombre;

    public AuthResponse() {}

    public AuthResponse(String token, String correo, String rol, String nombre) {
        this.token = token;
        this.correo = correo;
        this.rol = rol;
        this.nombre = nombre;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }
    public String getRol() { return rol; }
    public void setRol(String rol) { this.rol = rol; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
}
