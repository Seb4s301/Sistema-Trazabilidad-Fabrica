package com.latin.dream.erp.application.service;

import com.latin.dream.erp.crosscutting.exception.BusinessException;
import com.latin.dream.erp.domain.entity.Despacho;
import com.latin.dream.erp.domain.repository.DespachoRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

/**
 * Servicio de Aplicación: Gestión y trazabilidad de Despachos y Transporte.
 * Emplea Apache Commons (lang3) StringUtils para normalización de estados y textos.
 */
@Service
@Transactional
public class DespachoService {

    private static final Logger log = LoggerFactory.getLogger(DespachoService.class);
    private final DespachoRepository despachoRepository;

    public DespachoService(DespachoRepository despachoRepository) {
        this.despachoRepository = despachoRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getDespachosData() {
        log.info("Consultando datos de despachos y flota en tránsito...");
        List<Despacho> list = despachoRepository.findAllByOrderByCreadoEnDesc();

        LocalDate d = LocalDate.now();
        String[] meses = {"enero", "febrero", "marzo", "abril", "mayo", "junio", "julio", "agosto", "setiembre", "octubre", "noviembre", "diciembre"};
        String textoFecha = d.getDayOfMonth() + " " + meses[d.getMonthValue() - 1] + " " + d.getYear();

        int totalUnidades = 0;
        int transitoCount = 0;
        int entregadosCount = 0;
        int programadosCount = 0;

        List<Map<String, Object>> despachosDto = new ArrayList<>();
        List<Map<String, Object>> historialDto = new ArrayList<>();

        for (Despacho desp : list) {
            String est = StringUtils.lowerCase(StringUtils.defaultIfBlank(desp.getEstado(), "programado"));
            int u = desp.getUnidades() != null ? desp.getUnidades() : 0;
            totalUnidades += u;

            if ("transito".equals(est) || "en_transito".equals(est)) {
                transitoCount++;
                est = "transito";
            } else if ("entregado".equals(est)) {
                entregadosCount++;
            } else {
                programadosCount++;
                est = "programado";
            }

            String horaSalida = desp.getFechaSalida() != null ? "08:00" : "11:00";
            String horaEntrega = desp.getFechaEntregaEstimada() != null ? "13:30" : "14:00";

            Map<String, Object> dto = new LinkedHashMap<>();
            dto.put("codigo", desp.getCodigo());
            dto.put("lote", "LT-2610 · Lote asociado");
            dto.put("cliente", desp.getCliente());
            dto.put("destino", desp.getDestino());
            dto.put("unidades", u);
            dto.put("vehiculo", StringUtils.defaultIfBlank(desp.getVehiculo(), "LD-201"));
            dto.put("conductor", StringUtils.defaultIfBlank(desp.getConductor(), "Conductor Asignado"));
            dto.put("salida", horaSalida);
            dto.put("entregaEstimada", horaEntrega);
            dto.put("estado", est);
            despachosDto.add(dto);

            Map<String, Object> hist = new LinkedHashMap<>();
            hist.put("despacho", desp.getCodigo());
            hist.put("cliente", desp.getCliente());
            hist.put("unidades", u);
            hist.put("estado", StringUtils.capitalize(est));
            hist.put("salida", horaSalida);
            hist.put("entrega", horaEntrega);
            hist.put("vehiculo", StringUtils.defaultIfBlank(desp.getVehiculo(), "LD-201"));
            historialDto.add(hist);
        }

        int totalProgramados = list.size();

        List<Map<String, Object>> kpis = List.of(
            Map.of("etiqueta", "Despachos del día", "valor", String.valueOf(totalProgramados), "nota", transitoCount + " rutas activas en operación"),
            Map.of("etiqueta", "Unidades en ruta", "valor", String.valueOf(totalUnidades), "nota", totalProgramados + " vehículos asignados"),
            Map.of("etiqueta", "En tránsito", "valor", String.valueOf(transitoCount), "nota", "Rutas activas en carretera", "tipo", "transito"),
            Map.of("etiqueta", "Entregados", "valor", String.valueOf(entregadosCount), "nota", "Entregas confirmadas hoy", "tipo", "entregado")
        );

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("tituloFecha", "Despachos del " + textoFecha);
        resp.put("resumen", totalProgramados + " despachos programados · " + totalUnidades + " colchones · " + transitoCount + " en tránsito y " + entregadosCount + " entregado");
        resp.put("banner", "Sistema conectado con GPS y flota en tiempo real de Latin Dream.");
        resp.put("kpis", kpis);
        resp.put("despachos", despachosDto);
        resp.put("historial", historialDto);

        return resp;
    }

    public Map<String, Object> actualizarEstado(String codigo, String nuevoEstado) {
        log.info("Actualizando estado de despacho {} a {}", codigo, nuevoEstado);
        Despacho desp = despachoRepository.findByCodigo(codigo)
            .orElseThrow(() -> new BusinessException("DESPACHO_NO_ENCONTRADO", "No se encontró el despacho con código " + codigo));

        desp.setEstado(StringUtils.lowerCase(nuevoEstado));
        despachoRepository.save(desp);

        return Map.of(
            "codigo", desp.getCodigo(),
            "estado", desp.getEstado(),
            "mensaje", "Estado actualizado con éxito"
        );
    }
}
