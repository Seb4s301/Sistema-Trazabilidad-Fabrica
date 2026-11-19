package com.latin.dream.erp.application.dto;
import jakarta.validation.constraints.*;
public record ModeloRequest(@NotBlank @Size(max=20) String codigo,@NotBlank @Size(max=100) String nombre,
    @NotBlank @Size(max=50) String categoria,@NotBlank @Size(max=100) String dimensiones,
    @NotBlank @Size(max=100) String relleno,@NotNull Boolean activo) {}
