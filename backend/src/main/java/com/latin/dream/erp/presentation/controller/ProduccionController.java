package com.latin.dream.erp.presentation.controller;

import com.latin.dream.erp.application.service.ProduccionService;
import com.latin.dream.erp.crosscutting.annotation.UsuarioActual;
import com.latin.dream.erp.domain.entity.Usuario;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("${api.prefix}/produccion")
public class ProduccionController {

    private final ProduccionService produccionService;

    public ProduccionController(ProduccionService produccionService) {
        this.produccionService = produccionService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getProduccion() {
        return ResponseEntity.ok(produccionService.getProduccionData());
    }

    @GetMapping("/lotes")
    public ResponseEntity<List<Map<String, Object>>> getLotes() {
        return ResponseEntity.ok(produccionService.getLotes());
    }

    @GetMapping("/lotes/{id}/historial")
    public ResponseEntity<?> historial(@PathVariable String id) { return ResponseEntity.ok(produccionService.historialLote(id)); }

    @PostMapping("/avances")
    public ResponseEntity<Map<String, Object>> registrarAvance(@jakarta.validation.Valid @RequestBody com.latin.dream.erp.application.dto.AvanceRequest payload,
                                                               @UsuarioActual Usuario usuario) {
        return ResponseEntity.ok(produccionService.registrarAvance(payload, usuario));
    }
}
