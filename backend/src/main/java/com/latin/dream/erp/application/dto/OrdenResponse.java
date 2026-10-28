package com.latin.dream.erp.application.dto;

import java.time.LocalDate;

/**
 * DTO de salida de una Orden de fabricación.
 */
public class OrdenResponse {

    private String id;              // UUID
    private String numero;          // OP-AAAA-NNNN
    private String modelo;          // nombre del modelo para frontend
    private String modeloNombre;
    private String modeloCodigo;
    private String categoria;
    private Integer cantidad;
    private String prioridad;
    private LocalDate fechaEntrega;
    private String entrega;         // Formato fecha amigable para el frontend (ej. 24 oct. 2026)
    private String estado;
    private String etapaInicial;    // ej. "Pendiente de corte"
    private String lote;            // ej. "LT-2610-0420"
    private String registradoPor;
    private String turno;

    // Getters y setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNumero() { return numero; }
    public void setNumero(String numero) { this.numero = numero; }
    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public String getModeloNombre() { return modeloNombre; }
    public void setModeloNombre(String modeloNombre) { this.modeloNombre = modeloNombre; }
    public String getModeloCodigo() { return modeloCodigo; }
    public void setModeloCodigo(String modeloCodigo) { this.modeloCodigo = modeloCodigo; }
    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }
    public Integer getCantidad() { return cantidad; }
    public void setCantidad(Integer cantidad) { this.cantidad = cantidad; }
    public String getPrioridad() { return prioridad; }
    public void setPrioridad(String prioridad) { this.prioridad = prioridad; }
    public LocalDate getFechaEntrega() { return fechaEntrega; }
    public void setFechaEntrega(LocalDate fechaEntrega) { this.fechaEntrega = fechaEntrega; }
    public String getEntrega() { return entrega; }
    public void setEntrega(String entrega) { this.entrega = entrega; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getEtapaInicial() { return etapaInicial; }
    public void setEtapaInicial(String etapaInicial) { this.etapaInicial = etapaInicial; }
    public String getLote() { return lote; }
    public void setLote(String lote) { this.lote = lote; }
    public String getRegistradoPor() { return registradoPor; }
    public void setRegistradoPor(String registradoPor) { this.registradoPor = registradoPor; }
    public String getTurno() { return turno; }
    public void setTurno(String turno) { this.turno = turno; }
}
