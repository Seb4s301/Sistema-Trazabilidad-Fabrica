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
    private final SaldosLoteService saldos;
    private final com.latin.dream.erp.domain.repository.LoteAvanceRepository avanceRepository;

    public DashboardService(OrdenRepository ordenRepository, LoteRepository loteRepository, com.latin.dream.erp.domain.repository.LoteAvanceRepository avanceRepository, SaldosLoteService saldos) {
        this.ordenRepository = ordenRepository;
        this.loteRepository = loteRepository;
        this.avanceRepository = avanceRepository;
        this.saldos=saldos;
    }

    public Map<String, Object> getDashboardData() {
        List<Orden> ordenes = ordenRepository.findAll();
        List<Lote> lotes = loteRepository.findAll().stream().filter(l -> !Set.of("completado","cancelado").contains(l.getOrden().getEstado())).toList();
        var balances=saldos.calcular(lotes);

        int totalColchones = 0;
        int retrasos = 0;
        int enProcesoCount = 0;
        int pendientesCorteCount = 0;
        int listasDespachoCount = 0;

        LocalDate hoy = LocalDate.now();

        for (Orden o : ordenes) {
            if ("en_proceso".equalsIgnoreCase(o.getEstado()) || "calidad".equalsIgnoreCase(o.getEstado())) enProcesoCount++;
            else if ("pendiente_corte".equalsIgnoreCase(o.getEstado())) pendientesCorteCount++;
            else if ("listo_despacho".equalsIgnoreCase(o.getEstado())) listasDespachoCount++;
        }

        for (Lote l : lotes) {
            if ("Listo".equalsIgnoreCase(l.getAreaActual())) totalColchones += balances.get(l.getId()).disponibles();
            if ((l.getFechaEntrega() != null && l.getFechaEntrega().isBefore(hoy) && !"Listo".equalsIgnoreCase(l.getAreaActual()))) {
                retrasos++;
            }
        }

        List<Map<String, Object>> kpis = List.of(
            Map.of("etiqueta", "Unidades disponibles para despacho", "valor", String.valueOf(totalColchones), "nota", "Saldo sin reservar de lotes aprobados"),
            Map.of("etiqueta", "Órdenes en producción", "valor", String.valueOf(enProcesoCount), "nota", "Incluye control de calidad"),
            Map.of("etiqueta", "Lotes con retraso", "valor", String.valueOf(retrasos), "nota", retrasos > 0 ? "Requieren seguimiento" : "Al día", "notaTipo", retrasos > 0 ? "danger" : "success"),
            Map.of("etiqueta", "Órdenes pendientes de corte", "valor", String.valueOf(pendientesCorteCount), "nota", "Órdenes confirmadas")
        );
        LocalDate lunes = hoy.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        var avances = avanceRepository.findAllByOrderByInicioDesc();
        List<Map<String, Object>> ritmo = new ArrayList<>();
        String[] dias = {"Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom"};
        for (int i = 0; i < 7; i++) {
            LocalDate dia = lunes.plusDays(i);
            int unidades = avances.stream().filter(a -> "Listo".equalsIgnoreCase(a.getAreaDestino()))
                .filter(a -> a.getFin() != null && dia.equals(a.getFin().toLocalDate()))
                .mapToInt(a -> a.getLote().getUnidades()).sum();
            ritmo.add(Map.of("dia", dias[i], "valor", unidades, "meta", 0));
        }
        int totalActivas = enProcesoCount + pendientesCorteCount + listasDespachoCount;

        List<Map<String, Object>> donut = List.of(
            Map.of("label", "En proceso", "valor", enProcesoCount, "color", "#D4A935"),
            Map.of("label", "Pendientes de corte", "valor", pendientesCorteCount, "color", "#1B1D21"),
            Map.of("label", "Listas para despacho", "valor", listasDespachoCount, "color", "#1E7B4F")
        );

        Map<String, String> etapas = lotes.stream().collect(java.util.stream.Collectors.toMap(
            l -> l.getOrden().getId(), Lote::getAreaActual, (a, c) -> a));
        List<Map<String, Object>> seguimiento = new ArrayList<>();
        for (Orden o : ordenes.stream().filter(o -> !Set.of("borrador", "cancelado", "completado").contains(o.getEstado()))
                .sorted(Comparator.comparing(Orden::getFechaEntrega)).limit(6).toList()) {
            String modeloDesc = o.getModelo() != null
                ? o.getModelo().getNombre() + " · " + o.getModelo().getCategoria()
                : "Colchón";

            String estadoDesc = "En plazo";
            String estadoClase = "warning";
            if (o.getFechaEntrega() != null && o.getFechaEntrega().isBefore(hoy)
                    && !Set.of("listo_despacho", "completado", "cancelado").contains(o.getEstado())) {
                estadoDesc = "Retrasado";
                estadoClase = "danger";
            } else if ("listo_despacho".equalsIgnoreCase(o.getEstado()) || "completado".equalsIgnoreCase(o.getEstado())) {
                estadoDesc = "Completado";
                estadoClase = "success";
            }

            String etapa = etapas.getOrDefault(o.getId(), switch (o.getEstado().toLowerCase()) {
                case "pendiente_corte" -> "Corte";
                case "en_proceso" -> "Ensamblaje";
                case "calidad" -> "Calidad";
                case "listo_despacho" -> "Listo para despacho";
                default -> "Producción";
            });

            String entregaTexto = Fechas.corta(o.getFechaEntrega());

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

        int semanaNum = LocalDate.now().get(WeekFields.ISO.weekOfWeekBasedYear());

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("kpis", kpis);
        resp.put("ritmo", ritmo);
        resp.put("maxRitmo", Math.max(1, ritmo.stream().mapToInt(r -> (Integer) r.get("valor")).max().orElse(0)));
        resp.put("donut", donut);
        resp.put("totalOrdenesActivas", totalActivas);
        resp.put("seguimiento", seguimiento);
        resp.put("semana", semanaNum);

        return resp;
    }
}
