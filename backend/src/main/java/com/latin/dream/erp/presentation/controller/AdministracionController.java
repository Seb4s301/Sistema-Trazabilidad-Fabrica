package com.latin.dream.erp.presentation.controller;
import com.latin.dream.erp.application.service.CatalogoGestionService;
import com.latin.dream.erp.application.dto.*;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("${api.prefix}/administracion")
public class AdministracionController {
    private final CatalogoGestionService service;
    public AdministracionController(CatalogoGestionService service) { this.service=service; }
    @GetMapping public Object listar() { return service.listar(); }
    @PostMapping("/modelos") public Object crearModelo(@Valid @RequestBody ModeloRequest r) { return service.modelo(null,r); }
    @PutMapping("/modelos/{id}") public Object editarModelo(@PathVariable String id,@Valid @RequestBody ModeloRequest r) { return service.modelo(id,r); }
    @PostMapping("/tarifas") public Object crearTarifa(@Valid @RequestBody TarifaRequest r) { return service.tarifa(null,r); }
    @PutMapping("/tarifas/{id}") public Object editarTarifa(@PathVariable String id,@Valid @RequestBody TarifaRequest r) { return service.tarifa(id,r); }
}
