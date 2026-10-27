package com.latin.dream.erp.presentation.controller;

import com.latin.dream.erp.application.dto.OrdenRequest;
import com.latin.dream.erp.application.dto.OrdenResponse;
import com.latin.dream.erp.application.service.OrdenService;
import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.crosscutting.annotation.UsuarioActual;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Controller de presentación: Órdenes.
 * GET/POST/PUT /api/v1/ordenes
 * POST /api/v1/ordenes/{id}/crear
 */
@RestController
@RequestMapping("${api.prefix}/ordenes")
public class OrdenController {

    @Autowired
    private OrdenService ordenService;

    @GetMapping
    public ResponseEntity<List<OrdenResponse>> listar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String prioridad) {
        return ResponseEntity.ok(ordenService.listar(estado, prioridad));
    }

    @GetMapping("/{numero}")
    public ResponseEntity<OrdenResponse> buscarPorNumero(@PathVariable String numero) {
        return ResponseEntity.ok(ordenService.buscarPorNumero(numero));
    }

    @PostMapping("/borrador")
    public ResponseEntity<OrdenResponse> guardarBorrador(@Valid @RequestBody OrdenRequest request,
                                                         @UsuarioActual Usuario usuario) {
        return ResponseEntity.ok(ordenService.guardarBorrador(request, usuario));
    }

    @PostMapping("/{id}/crear")
    public ResponseEntity<OrdenResponse> crear(@PathVariable String id,
                                               @UsuarioActual Usuario usuario) {
        return ResponseEntity.ok(ordenService.crearOrden(id, usuario));
    }
}
