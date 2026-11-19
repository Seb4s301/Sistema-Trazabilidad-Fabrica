package com.latin.dream.erp.application.service;

import com.latin.dream.erp.domain.entity.*;
import com.latin.dream.erp.domain.repository.LiquidacionComisionRepository;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import java.time.YearMonth;
import com.latin.dream.erp.domain.entity.PrecioComision;
import com.latin.dream.erp.domain.repository.OrdenRepository;
import com.latin.dream.erp.domain.repository.PrecioComisionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Transactional(readOnly = true)
public class ComisionesService {
    private final PrecioComisionRepository precioRepository;
    private final OrdenRepository ordenRepository;
    private final LiquidacionComisionRepository liquidaciones;

    public ComisionesService(PrecioComisionRepository precioRepository, OrdenRepository ordenRepository, LiquidacionComisionRepository liquidaciones) {
        this.precioRepository = precioRepository;
        this.ordenRepository = ordenRepository;
        this.liquidaciones=liquidaciones;
    }

    // Las estimaciones se congelan al liquidar una orden totalmente entregada.
    public List<Map<String,Object>> calcularFilas() { return calcularFilas(YearMonth.now()); }
    public List<Map<String, Object>> calcularFilas(YearMonth periodo) {
        LocalDate inicio = periodo.atDay(1);
        Map<String,LiquidacionComision> porOrden=new HashMap<>();
        liquidaciones.findByPeriodo(periodo.toString()).forEach(l -> porOrden.put(l.getOrden().getId(),l));
        LocalDate fin = inicio.plusMonths(1);
        List<PrecioComision> precios = precioRepository.findAll();
        List<Map<String, Object>> filas = new ArrayList<>();
        for (Orden orden : ordenRepository.findAll()) {
            if (Set.of("borrador", "cancelado").contains(orden.getEstado())) continue;
            LocalDate fecha = orden.getCreadoEn().toLocalDate();
            if (fecha.isBefore(inicio) || !fecha.isBefore(fin)) continue;
            PrecioComision tarifa = precios.stream()
                .filter(p -> p.getModelo().getId().equals(orden.getModelo().getId()))
                .filter(p -> p.getCategoria().equals(orden.getModelo().getCategoria()))
                .filter(p -> !p.getVigenteDesde().isAfter(fecha)
                    && (p.getVigenteHasta() == null || !p.getVigenteHasta().isBefore(fecha)))
                .max(Comparator.comparing(PrecioComision::getVigenteDesde).thenComparing(PrecioComision::getId)).orElse(null);
            BigDecimal base = tarifa == null ? BigDecimal.ZERO : tarifa.getPrecioBase();
            BigDecimal porcentaje = tarifa == null ? BigDecimal.ZERO : tarifa.getPorcentaje();
            BigDecimal monto = base.multiply(porcentaje).multiply(BigDecimal.valueOf(orden.getCantidad()))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            Map<String, Object> fila = new LinkedHashMap<>();
            fila.put("ordenId",orden.getId());
            fila.put("liquidacionId",null);
            fila.put("puedeLiquidar", "completado".equals(orden.getEstado()) && tarifa!=null);
            fila.put("referenciaPago",null);
            fila.put("tipo", orden.getNumero());
            fila.put("modelo", orden.getModelo().getNombre());
            fila.put("categoria", orden.getModelo().getCategoria());
            fila.put("relleno", orden.getModelo().getRelleno());
            fila.put("unidades", orden.getCantidad());
            fila.put("precioBase", base);
            fila.put("porcentaje", porcentaje);
            fila.put("monto", monto);
            fila.put("estado", tarifa == null ? "Sin tarifa" : "Estimado");
            LiquidacionComision l=porOrden.get(orden.getId());
            if (l!=null) {
                fila.put("liquidacionId",l.getId()); fila.put("modelo",l.getModelo()); fila.put("categoria",l.getCategoria()); fila.put("relleno",l.getRelleno());
                fila.put("unidades",l.getUnidades()); fila.put("precioBase",l.getPrecioBase()); fila.put("porcentaje",l.getPorcentaje()); fila.put("monto",l.getMonto());
                fila.put("estado",l.getPagadoEn()==null?"Pendiente":"Pagado"); fila.put("puedeLiquidar",false);
                fila.put("liquidadoPor",l.getLiquidadoPor().getNombre()); fila.put("pagadoPor",l.getPagadoPor()==null?null:l.getPagadoPor().getNombre()); fila.put("liquidadoEn",l.getLiquidadoEn()); fila.put("pagadoEn",l.getPagadoEn()); fila.put("referenciaPago",l.getReferenciaPago());
            }
            filas.add(fila);
        }
        return filas;
    }

    public Map<String,Object> getComisionesData() { return getComisionesData(YearMonth.now()); }
    public Map<String, Object> getComisionesData(YearMonth mes) {
        List<Map<String, Object>> filas = calcularFilas(mes);
        BigDecimal total = BigDecimal.ZERO;
        int unidades = 0;
        Map<String, BigDecimal> modelos = new TreeMap<>();
        Map<String, BigDecimal> rellenos = new TreeMap<>();
        for (Map<String, Object> fila : filas) {
            BigDecimal monto = (BigDecimal) fila.get("monto");
            total = total.add(monto);
            unidades += (Integer) fila.get("unidades");
            modelos.merge((String) fila.get("modelo"), monto, BigDecimal::add);
            rellenos.merge((String) fila.get("relleno"), monto, BigDecimal::add);
        }
        String periodo = mes.atDay(1).format(DateTimeFormatter.ofPattern("MMMM uuuu", Locale.forLanguageTag("es-PE")));
        var mayor = modelos.entrySet().stream().max(Map.Entry.comparingByValue());
        BigDecimal promedio = unidades == 0 ? BigDecimal.ZERO : total.divide(BigDecimal.valueOf(unidades), 2, RoundingMode.HALF_UP);
        List<Map<String, Object>> kpis = List.of(
            Map.of("etiqueta", "Comisiones estimadas del mes ($)", "valor", dinero(total), "nota", periodo + " · " + filas.size() + " órdenes"),
            Map.of("etiqueta", "Pendiente de pago ($)", "valor", dinero(filas.stream().filter(f -> "Pendiente".equals(f.get("estado"))).map(f -> (BigDecimal)f.get("monto")).reduce(BigDecimal.ZERO,BigDecimal::add)), "nota", "Solo importes liquidados", "notaTipo", "warning"),
            Map.of("etiqueta", "Modelo con mayor comisión estimada", "valor", mayor.map(Map.Entry::getKey).orElse("—"), "nota", mayor.map(e -> dinero(e.getValue())).orElse("Sin órdenes")),
            Map.of("etiqueta", "Promedio estimado por unidad ($)", "valor", dinero(promedio), "nota", unidades + " unidades · " + periodo)
        );
        return Map.of("kpis", kpis, "filas", filas, "distribucionModelo", distribucion(modelos),
            "distribucionRelleno", distribucion(rellenos), "maxModelo", maximo(modelos), "maxRelleno", maximo(rellenos));
    }

    @Transactional
    public Map<String,Object> liquidar(String ordenId,Usuario usuario) {
        Orden o=ordenRepository.bloquearPorId(ordenId).orElseThrow(() -> new BusinessException("ORDEN_NO_ENCONTRADA","La orden no existe"));
        var previa=liquidaciones.findByOrdenId(ordenId);
        YearMonth mes=YearMonth.from(o.getCreadoEn());
        if (previa.isPresent()) return calcularFilas(mes).stream().filter(f -> ordenId.equals(f.get("ordenId"))).findFirst().orElseThrow();
        if (!"completado".equals(o.getEstado())) throw new BusinessException("ENTREGA_PENDIENTE","La orden debe estar totalmente entregada antes de liquidar");
        Map<String,Object> f=calcularFilas(mes).stream().filter(d -> ordenId.equals(d.get("ordenId"))).findFirst().orElseThrow();
        if ("Sin tarifa".equals(f.get("estado"))) throw new BusinessException("TARIFA_REQUERIDA","Registra una tarifa vigente para la fecha de la orden");
        LiquidacionComision l=new LiquidacionComision(); l.setOrden(o); l.setPeriodo(mes.toString()); l.setModelo((String)f.get("modelo"));
        l.setCategoria((String)f.get("categoria")); l.setRelleno((String)f.get("relleno")); l.setUnidades((Integer)f.get("unidades"));
        l.setPrecioBase((BigDecimal)f.get("precioBase")); l.setPorcentaje((BigDecimal)f.get("porcentaje")); l.setMonto((BigDecimal)f.get("monto"));
        l.setLiquidadoEn(java.time.LocalDateTime.now()); l.setLiquidadoPor(usuario); liquidaciones.saveAndFlush(l);
        return calcularFilas(mes).stream().filter(d -> ordenId.equals(d.get("ordenId"))).findFirst().orElseThrow();
    }
    @Transactional
    public void pagar(String id,String referencia,Usuario usuario) {
        if (referencia==null || referencia.isBlank() || referencia.length()>200) throw new BusinessException("REFERENCIA_REQUERIDA","Indica la referencia del pago (máximo 200 caracteres)");
        LiquidacionComision l=liquidaciones.bloquearPorId(id).orElseThrow(() -> new BusinessException("LIQUIDACION_NO_ENCONTRADA","La liquidación no existe"));
        if (l.getPagadoEn()!=null) {
            if (!referencia.trim().equals(l.getReferenciaPago())) throw new BusinessException("PAGO_YA_REGISTRADO","La liquidación ya tiene un pago registrado");
            return;
        }
        l.setPagadoEn(java.time.LocalDateTime.now()); l.setPagadoPor(usuario); l.setReferenciaPago(referencia.trim()); liquidaciones.save(l);
    }
    public static YearMonth periodo(String valor) {
        try { return valor==null || valor.isBlank()?YearMonth.now():YearMonth.parse(valor); }
        catch (java.time.DateTimeException e) { throw new BusinessException("PERIODO_INVALIDO","Usa un período con formato AAAA-MM"); }
    }

    private List<Map<String, Object>> distribucion(Map<String, BigDecimal> valores) {
        return valores.entrySet().stream().map(e -> Map.<String, Object>of("label", e.getKey(), "valor", e.getValue())).toList();
    }
    private BigDecimal maximo(Map<String, BigDecimal> valores) {
        return valores.values().stream().max(Comparator.naturalOrder()).orElse(BigDecimal.ONE).max(BigDecimal.ONE);
    }
    private String dinero(BigDecimal valor) { return "$" + String.format(Locale.forLanguageTag("es-PE"), "%,.2f", valor); }
}
