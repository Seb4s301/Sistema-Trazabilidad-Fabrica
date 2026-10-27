package com.latin.dream.erp.crosscutting.exception;

import com.latin.dream.erp.domain.entity.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import java.util.HashMap;
import java.util.Map;

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
        return ResponseEntity.badRequest().body(error);
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

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorRespuesta> manejarErrorGeneral(Exception ex) {
        ErrorRespuesta error = new ErrorRespuesta("ERROR_INTERNO", "Ocurrió un error interno del servidor");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * Resuelve el usuario autenticado del contexto de seguridad (para auditar).
     */
    protected Usuario usuarioAutenticado() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario) {
            return (Usuario) auth.getPrincipal();
        }
        return null;
    }

    public static record ErrorRespuesta(String codigo, String mensaje) {}
}
