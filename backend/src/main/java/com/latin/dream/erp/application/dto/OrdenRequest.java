package com.latin.dream.erp.application.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO de entrada para crear / actualizar una Orden de fabricación.
 */
public class OrdenRequest {

    private String modeloId; // UUID del modelo

    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad;

    @Pattern(regexp = "Alta|Normal|Baja", message = "Prioridad inválida")
    private String prioridad; // Alta, Normal, Baja

    private LocalDate fechaEntrega;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

    @Pattern(regexp = "[0-9a-fA-F-]{36}")
    private String solicitudId;

    public String getSolicitudId() { return solicitudId; }
    public void setSolicitudId(String solicitudId) { this.solicitudId = solicitudId; }

    // Getters y setters
    public String getModeloId() { return modeloId; }
    public void setModeloId(String modeloId) { this.modeloId = modeloId; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}
