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

    @Autowired private com.latin.dream.erp.application.service.HistorialService historial;

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
    @PostMapping
    public ResponseEntity<OrdenResponse> crearDirecta(@Valid @RequestBody OrdenRequest request, @UsuarioActual Usuario usuario) {
        return ResponseEntity.ok(ordenService.crear(request,usuario));
    }
    @PutMapping("/{id}/borrador")
    public ResponseEntity<OrdenResponse> editar(@PathVariable String id,@Valid @RequestBody OrdenRequest request,@UsuarioActual Usuario usuario) {
        return ResponseEntity.ok(ordenService.editarBorrador(id,request,usuario));
    }
    @DeleteMapping("/{id}/borrador")
    public ResponseEntity<Void> eliminar(@PathVariable String id) { ordenService.eliminarBorrador(id); return ResponseEntity.noContent().build(); }
    @GetMapping("/{numero}/historial")
    public ResponseEntity<?> historial(@PathVariable String numero) { return ResponseEntity.ok(historial.consultar(ordenService.buscarPorNumero(numero).getId())); }
}
