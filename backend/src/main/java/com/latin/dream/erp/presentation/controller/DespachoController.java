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
                                                                @RequestBody Map<String, String> payload) {
        String estado = payload.get("estado");
        return ResponseEntity.ok(despachoService.actualizarEstado(codigo, estado));
    }
}
