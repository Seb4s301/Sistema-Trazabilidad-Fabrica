package com.latin.dream.erp.presentation.controller;

import com.latin.dream.erp.application.service.ComisionesService;
import com.latin.dream.erp.application.service.ReporteExcelService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller de presentación: Comisiones por producto.
 * Principio de Responsabilidad Única (SRP): expone los endpoints HTTP para consulta
 * y exportación de comisiones.
 */
@RestController
@RequestMapping("${api.prefix}/comisiones")
public class ComisionesController {

    private final ComisionesService comisionesService;
    private final ReporteExcelService reporteExcelService;

    public ComisionesController(ComisionesService comisionesService,
                                ReporteExcelService reporteExcelService) {
        this.comisionesService = comisionesService;
        this.reporteExcelService = reporteExcelService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getComisiones(@RequestParam(required=false) String periodo) {
        return ResponseEntity.ok(comisionesService.getComisionesData(ComisionesService.periodo(periodo)));
    }

    @GetMapping("/reporte/excel")
    public ResponseEntity<byte[]> exportarReporteExcel(@RequestParam(required=false) String periodo) {
        byte[] excelBytes = reporteExcelService.exportarReporteComisiones(ComisionesService.periodo(periodo));
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"Reporte_Comisiones_LatinDream.xlsx\"")
            .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
            .body(excelBytes);
    }
    @PostMapping("/ordenes/{id}/liquidar")
    public ResponseEntity<?> liquidar(@PathVariable String id,@com.latin.dream.erp.crosscutting.annotation.UsuarioActual com.latin.dream.erp.domain.entity.Usuario usuario) {
        return ResponseEntity.ok(comisionesService.liquidar(id,usuario));
    }
    @PostMapping("/liquidaciones/{id}/pago")
    public ResponseEntity<?> pagar(@PathVariable String id,@RequestBody Map<String,String> body,
            @com.latin.dream.erp.crosscutting.annotation.UsuarioActual com.latin.dream.erp.domain.entity.Usuario usuario) {
        comisionesService.pagar(id,body.get("referencia"),usuario); return ResponseEntity.ok(Map.of("mensaje","Pago registrado"));
    }
}
