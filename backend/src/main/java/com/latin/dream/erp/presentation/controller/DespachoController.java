package com.latin.dream.erp.presentation.controller;

import com.latin.dream.erp.application.service.DespachoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/despachos")
public class DespachoController {

    private final DespachoService despachoService;

    public DespachoController(DespachoService despachoService) {
        this.despachoService = despachoService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getDespachos() {
        return ResponseEntity.ok(despachoService.getDespachosData());
    }

    @PatchMapping("/{codigo}/estado")
    public ResponseEntity<Map<String, Object>> actualizarEstado(@PathVariable String codigo,
                                                                @RequestBody Map<String, String> payload,
            @com.latin.dream.erp.crosscutting.annotation.UsuarioActual com.latin.dream.erp.domain.entity.Usuario usuario) {
        String estado = payload.get("estado");
        return ResponseEntity.ok(despachoService.actualizarEstado(codigo, estado, usuario));
    }
    @GetMapping("/lotes-disponibles")
    public ResponseEntity<?> disponibles() { return ResponseEntity.ok(despachoService.disponibles()); }
    @PostMapping
    public ResponseEntity<?> crear(@jakarta.validation.Valid @RequestBody com.latin.dream.erp.application.dto.DespachoRequest request,
            @com.latin.dream.erp.crosscutting.annotation.UsuarioActual com.latin.dream.erp.domain.entity.Usuario usuario) {
        return ResponseEntity.ok(despachoService.crear(request,usuario));
    }
    @PutMapping("/{codigo}")
    public ResponseEntity<?> editar(@PathVariable String codigo,@jakarta.validation.Valid @RequestBody com.latin.dream.erp.application.dto.DespachoRequest request,
            @com.latin.dream.erp.crosscutting.annotation.UsuarioActual com.latin.dream.erp.domain.entity.Usuario usuario) {
        return ResponseEntity.ok(despachoService.editar(codigo,request,usuario));
    }
}
