package com.latin.dream.erp.application.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

/**
 * DTO de entrada para crear / actualizar una Orden de fabricación.
 */
public class OrdenRequest {

    @NotNull(message = "El modelo es obligatorio")
    private String modeloId; // UUID del modelo

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    private Integer cantidad;

    @NotBlank(message = "La prioridad es obligatoria")
    private String prioridad; // Alta, Normal, Baja

    @NotNull(message = "La fecha de entrega es obligatoria")
    private LocalDate fechaEntrega;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
    private String observaciones;

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
