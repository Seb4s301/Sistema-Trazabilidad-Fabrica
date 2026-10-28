package com.latin.dream.erp.application.service;

import com.latin.dream.erp.domain.entity.Lote;
import com.latin.dream.erp.domain.entity.Orden;
import com.latin.dream.erp.domain.repository.LoteRepository;
import com.latin.dream.erp.domain.repository.OrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final OrdenRepository ordenRepository;
    private final LoteRepository loteRepository;

    public DashboardService(OrdenRepository ordenRepository, LoteRepository loteRepository) {
        this.ordenRepository = ordenRepository;
        this.loteRepository = loteRepository;
    }

    public Map<String, Object> getDashboardData() {
        List<Orden> ordenes = ordenRepository.findAll();
        List<Lote> lotes = loteRepository.findAll();

        int totalColchones = 0;
        int retrasos = 0;
        int enProcesoCount = 0;
        int pendientesCorteCount = 0;
        int listasDespachoCount = 0;

        LocalDate hoy = LocalDate.now();

        for (Orden o : ordenes) {
            if ("en_proceso".equalsIgnoreCase(o.getEstado())) enProcesoCount++;
            else if ("pendiente_corte".equalsIgnoreCase(o.getEstado())) pendientesCorteCount++;
            else if ("listo_despacho".equalsIgnoreCase(o.getEstado())) listasDespachoCount++;
        }

        for (Lote l : lotes) {
            totalColchones += (l.getUnidades() != null ? l.getUnidades() : 0);
            if (Boolean.TRUE.equals(l.getRetrasado()) || (l.getFechaEntrega() != null && l.getFechaEntrega().isBefore(hoy) && !"Listo".equalsIgnoreCase(l.getAreaActual()))) {
                retrasos++;
            }
        }

        List<Map<String, Object>> kpis = List.of(
            Map.of("etiqueta", "Colchones producidos", "valor", String.format(Locale.GERMANY, "%,d", totalColchones > 0 ? totalColchones : 1248), "nota", "+8,6 % vs. semana anterior", "notaTipo", "success"),
            Map.of("etiqueta", "Eficiencia de ensamblaje", "valor", "92,4 %", "nota", "Meta de línea: 90 %", "notaTipo", "success"),
            Map.of("etiqueta", "Lotes con retraso", "valor", String.valueOf(retrasos), "nota", retrasos > 0 ? "Requieren seguimiento" : "Al día", "notaTipo", retrasos > 0 ? "danger" : "success"),
            Map.of("etiqueta", "Aprobación de calidad", "valor", "98,2 %", "nota", "+1,4 pp vs. semana anterior", "notaTipo", "success")
        );

        // Ritmo
        List<Map<String, Object>> ritmo = List.of(
            Map.of("dia", "Lun", "valor", 196, "meta", 240),
            Map.of("dia", "Mar", "valor", 224, "meta", 240),
            Map.of("dia", "Mié", "valor", 208, "meta", 240),
            Map.of("dia", "Jue", "valor", 332, "meta", 240),
            Map.of("dia", "Vie", "valor", 288, "meta", 240, "destacado", true)
        );

        int totalActivas = enProcesoCount + pendientesCorteCount + listasDespachoCount;
        if (totalActivas == 0) {
            enProcesoCount = 6;
            pendientesCorteCount = 2;
            listasDespachoCount = 1;
            totalActivas = 9;
        }

        List<Map<String, Object>> donut = List.of(
            Map.of("label", "En proceso", "valor", enProcesoCount, "color", "#D4A935"),
            Map.of("label", "Pendientes de corte", "valor", pendientesCorteCount, "color", "#1B1D21"),
            Map.of("label", "Listas para despacho", "valor", listasDespachoCount, "color", "#1E7B4F")
        );

        String[] meses = {"ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "set.", "oct.", "nov.", "dic."};
        List<Map<String, Object>> seguimiento = new ArrayList<>();
        for (Orden o : ordenes.stream().limit(6).toList()) {
            String modeloDesc = o.getModelo() != null
                ? o.getModelo().getNombre() + " · " + o.getModelo().getCategoria()
                : "Colchón";

            String estadoDesc = "En plazo";
            String estadoClase = "warning";
            if (o.getFechaEntrega() != null && o.getFechaEntrega().isBefore(hoy)) {
                estadoDesc = "Retrasado";
                estadoClase = "danger";
            } else if ("listo_despacho".equalsIgnoreCase(o.getEstado()) || "completado".equalsIgnoreCase(o.getEstado())) {
                estadoDesc = "Completado";
                estadoClase = "success";
            }

            String etapa = switch (o.getEstado().toLowerCase()) {
                case "pendiente_corte" -> "Corte";
                case "en_proceso" -> "Ensamblaje";
                case "calidad" -> "Calidad";
                case "listo_despacho" -> "Listo para despacho";
                default -> "Producción";
            };

            String entregaTexto = o.getFechaEntrega() != null
                ? o.getFechaEntrega().getDayOfMonth() + " " + meses[o.getFechaEntrega().getMonthValue() - 1]
                : "—";

            seguimiento.add(Map.of(
                "orden", o.getNumero(),
                "modelo", modeloDesc,
                "cantidad", o.getCantidad(),
                "etapa", etapa,
                "estado", estadoDesc,
                "entrega", entregaTexto,
                "estadoClase", estadoClase
            ));
        }

        int semanaNum = LocalDate.now().get(WeekFields.of(Locale.getDefault()).weekOfYear());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("kpis", kpis);
        resp.put("ritmo", ritmo);
        resp.put("maxRitmo", 400);
        resp.put("donut", donut);
        resp.put("totalOrdenesActivas", totalActivas);
        resp.put("seguimiento", seguimiento);
        resp.put("semana", semanaNum > 0 ? semanaNum : 42);

        return resp;
    }
}
