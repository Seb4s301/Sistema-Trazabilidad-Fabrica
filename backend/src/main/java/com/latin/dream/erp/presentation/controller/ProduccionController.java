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

    @PostMapping("/avances")
    public ResponseEntity<Map<String, Object>> registrarAvance(@RequestBody Map<String, Object> payload,
                                                               @UsuarioActual Usuario usuario) {
        return ResponseEntity.ok(produccionService.registrarAvance(payload, usuario));
    }
}
