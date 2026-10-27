package com.latin.dream.erp.crosscutting.exception;

/**
 * Excepción de negocio de cross-cutting.
 * Lleva un código de error y un mensaje en español.
 */
public class BusinessException extends RuntimeException {

    private final String codigo;

    public BusinessException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
