package com.latin.dream.erp.application.service;

import com.latin.dream.erp.domain.entity.Modelo;
import com.latin.dream.erp.domain.entity.PrecioComision;
import com.latin.dream.erp.domain.repository.ModeloRepository;
import com.latin.dream.erp.domain.repository.PrecioComisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ComisionesService {

    private final PrecioComisionRepository precioComisionRepository;
    private final ModeloRepository modeloRepository;

    public ComisionesService(PrecioComisionRepository precioComisionRepository, ModeloRepository modeloRepository) {
        this.precioComisionRepository = precioComisionRepository;
        this.modeloRepository = modeloRepository;
    }

    public Map<String, Object> getComisionesData() {
        List<PrecioComision> precios = precioComisionRepository.findAll();

        List<Map<String, Object>> filas = new ArrayList<>();
        double totalComisiones = 0;
        double pendientes = 0;
        int totalUnidades = 0;

        Map<String, Double> porModelo = new LinkedHashMap<>();
        Map<String, Double> porRelleno = new LinkedHashMap<>();

        // Si hay registros en precios_comision, calcular a partir de ellos
        if (!precios.isEmpty()) {
            String[] estados = {"Pagado", "Pendiente", "Pendiente", "En revisión", "Pagado", "Pagado"};
            int[] demoUnidades = {120, 200, 80, 60, 150, 90};

            for (int i = 0; i < precios.size(); i++) {
                PrecioComision pc = precios.get(i);
                int u = i < demoUnidades.length ? demoUnidades[i] : 100;
                double base = pc.getPrecioBase().doubleValue();
                double pct = pc.getPorcentaje().doubleValue();
                double monto = (u * base * pct) / 100.0;
                String est = i < estados.length ? estados[i] : "Pendiente";

                totalComisiones += monto;
                if (!"Pagado".equalsIgnoreCase(est)) pendientes += monto;
                totalUnidades += u;

                String mNombre = pc.getModelo().getNombre();
                porModelo.put(mNombre, porModelo.getOrDefault(mNombre, 0.0) + monto);

                String relleno = pc.getModelo().getRelleno() != null ? pc.getModelo().getRelleno() : "Resortes Ensacados";
                porRelleno.put(relleno, porRelleno.getOrDefault(relleno, 0.0) + monto);

                Map<String, Object> fila = new LinkedHashMap<>();
                fila.put("tipo", "Modelo");
                fila.put("modelo", mNombre);
                fila.put("categoria", pc.getCategoria());
                fila.put("unidades", u);
                fila.put("precioBase", (int) base);
                fila.put("porcentaje", (int) pct);
                fila.put("monto", (int) monto);
                fila.put("estado", est);
                filas.add(fila);
            }
        } else {
            // Datos demo si la tabla aún no se ha sembrado
            filas = List.of(
                Map.of("tipo", "Modelo", "modelo", "Ortopedic Pro", "categoria", "Queen", "unidades", 120, "precioBase", 400, "porcentaje", 5, "monto", 2400, "estado", "Pagado"),
                Map.of("tipo", "Modelo", "modelo", "Dream Classic", "categoria", "2 Plazas", "unidades", 200, "precioBase", 250, "porcentaje", 4, "monto", 2000, "estado", "Pendiente"),
                Map.of("tipo", "Modelo", "modelo", "Comfort Plus", "categoria", "King", "unidades", 80, "precioBase", 500, "porcentaje", 6, "monto", 2400, "estado", "Pendiente"),
                Map.of("tipo", "Modelo", "modelo", "Ortopedic Pro", "categoria", "King", "unidades", 60, "precioBase", 500, "porcentaje", 5, "monto", 1500, "estado", "En revisión"),
                Map.of("tipo", "Modelo", "modelo", "Dream Classic", "categoria", "1.5 Plazas", "unidades", 150, "precioBase", 200, "porcentaje", 4, "monto", 1200, "estado", "Pagado"),
                Map.of("tipo", "Modelo", "modelo", "Comfort Plus", "categoria", "Queen", "unidades", 90, "precioBase", 400, "porcentaje", 6, "monto", 2160, "estado", "Pagado")
            );
            totalComisiones = 11660;
            pendientes = 5900;
            totalUnidades = 700;
            porModelo.put("Ortopedic Pro", 3900.0);
            porModelo.put("Dream Classic", 3200.0);
            porModelo.put("Comfort Plus", 4560.0);
            porRelleno.put("Resortes ensacados", 7100.0);
            porRelleno.put("Memory Foam", 4560.0);
        }

        List<Map<String, Object>> distModelo = porModelo.entrySet().stream()
            .map(e -> Map.<String, Object>of("label", e.getKey(), "valor", e.getValue().intValue()))
            .toList();

        List<Map<String, Object>> distRelleno = porRelleno.entrySet().stream()
            .map(e -> Map.<String, Object>of("label", e.getKey(), "valor", e.getValue().intValue()))
            .toList();

        double promedio = totalUnidades > 0 ? (totalComisiones / totalUnidades) : 16.66;

        List<Map<String, Object>> kpis = List.of(
            Map.of("etiqueta", "Total Comisiones del Mes ($)", "valor", "$" + String.format(Locale.GERMANY, "%,d", (int) totalComisiones), "nota", "Octubre 2026 · " + filas.size() + " filas"),
            Map.of("etiqueta", "Comisiones Pendientes de Pago ($)", "valor", "$" + String.format(Locale.GERMANY, "%,d", (int) pendientes), "nota", "Incluye montos en revisión", "notaTipo", "warning"),
            Map.of("etiqueta", "Modelo de Mayor Rendimiento", "valor", "Comfort Plus", "nota", "$4.560 · 2 categorías"),
            Map.of("etiqueta", "Promedio de Comisión por Unidad Vendida/Producida ($)", "valor", "$" + String.format(Locale.GERMANY, "%.2f", promedio), "nota", totalUnidades + " unidades · Octubre 2026")
        );

        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("kpis", kpis);
        resp.put("filas", filas);
        resp.put("distribucionModelo", distModelo);
        resp.put("distribucionRelleno", distRelleno);
        resp.put("maxModelo", 5000);
        resp.put("maxRelleno", 8000);

        return resp;
    }
}
