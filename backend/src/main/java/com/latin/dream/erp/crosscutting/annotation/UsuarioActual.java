package com.latin.dream.erp.crosscutting.annotation;

import java.lang.annotation.*;

/**
 * Anotación de cross-cutting: resuelve el usuario autenticado desde el contexto
 * de seguridad y lo inyecta como parámetro del controller.
 *
 * Uso: public ResponseEntity<X> metodo(@UsuarioActual Usuario usuario)
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface UsuarioActual {
}
