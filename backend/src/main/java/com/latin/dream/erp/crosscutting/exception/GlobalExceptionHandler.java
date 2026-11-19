package com.latin.dream.erp.crosscutting.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

/**
 * Controlador de cross-cutting: manejo global de errores.
 * Formato de respuesta: { "codigo": "...", "mensaje": "..." } en español.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorRespuesta> manejarBusinessException(BusinessException ex) {
        ErrorRespuesta error = new ErrorRespuesta(ex.getCodigo(), ex.getMessage());
        HttpStatus estado = ex.getCodigo().equals("CREDENCIALES_INVALIDAS") || ex.getCodigo().equals("USUARIO_INACTIVO")
            ? HttpStatus.UNAUTHORIZED : ex.getCodigo().endsWith("NO_ENCONTRADA") || ex.getCodigo().endsWith("NO_ENCONTRADO")
            ? HttpStatus.NOT_FOUND : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(estado).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorRespuesta> manejarValidacion(MethodArgumentNotValidException ex) {
        StringBuilder mensaje = new StringBuilder("Errores de validación: ");
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            mensaje.append(error.getField()).append(": ").append(error.getDefaultMessage()).append("; ");
        }
        ErrorRespuesta error = new ErrorRespuesta("VALIDACION_ERROR", mensaje.toString().trim());
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(org.springframework.dao.DataIntegrityViolationException.class)
    public ResponseEntity<ErrorRespuesta> manejarConflicto(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorRespuesta("CONFLICTO_DATOS", "La operación entra en conflicto con un registro existente. Actualiza los datos e inténtalo de nuevo"));
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorRespuesta> manejarErrorGeneral(Exception ex) {
        ErrorRespuesta error = new ErrorRespuesta("ERROR_INTERNO", "Ocurrió un error interno del servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(org.springframework.http.converter.HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRespuesta> manejarJsonInvalido(Exception ex) {
        return ResponseEntity.badRequest().body(new ErrorRespuesta("JSON_INVALIDO", "El contenido de la petición no es válido"));
    }

    public static record ErrorRespuesta(String codigo, String mensaje) {}
}
