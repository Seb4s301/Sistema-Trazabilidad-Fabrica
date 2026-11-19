package com.latin.dream.erp.application.service;

import com.latin.dream.erp.application.dto.DespachoRequest;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import com.latin.dream.erp.domain.entity.*;
import com.latin.dream.erp.domain.repository.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

@Service @Transactional
public class DespachoService {
    private final DespachoRepository despachos;
    private final DespachoLoteRepository asignaciones;
    private final LoteRepository lotes;
    private final HistorialService historial;
    private final JdbcTemplate jdbc;
    public DespachoService(DespachoRepository despachos, DespachoLoteRepository asignaciones,
            LoteRepository lotes, HistorialService historial, JdbcTemplate jdbc) {
        this.despachos=despachos; this.asignaciones=asignaciones; this.lotes=lotes; this.historial=historial; this.jdbc=jdbc;
    }

    @Transactional(readOnly=true)
    public Map<String,Object> getDespachosData() {
        var lista=despachos.findAllByOrderByCreadoEnDesc();
        var porDespacho=asignaciones.findAll().stream().collect(Collectors.groupingBy(a -> a.getDespacho().getCodigo()));
        List<Map<String,Object>> filas=lista.stream().map(d -> dto(d,porDespacho.getOrDefault(d.getCodigo(),List.of()))).toList();
        LocalDate hoy=LocalDate.now();
        long salidas=lista.stream().filter(d -> hoy.equals(d.getFechaSalida()) && !"cancelado".equals(d.getEstado())).count();
        long transito=lista.stream().filter(d -> "transito".equals(estado(d))).count();
        int unidades=lista.stream().filter(d -> "transito".equals(estado(d))).mapToInt(Despacho::getUnidades).sum();
        long entregados=lista.stream().filter(d -> d.getEntregadoEn()!=null && hoy.equals(d.getEntregadoEn().toLocalDate())).count();
        List<Map<String,Object>> kpis=List.of(
            Map.of("etiqueta","Salidas del día","valor",String.valueOf(salidas)),
            Map.of("etiqueta","Unidades en ruta","valor",String.valueOf(unidades)),
            Map.of("etiqueta","En tránsito","valor",String.valueOf(transito),"tipo","transito"),
            Map.of("etiqueta","Entregados hoy","valor",String.valueOf(entregados),"tipo","entregado"));
        List<Map<String,Object>> hist=filas.stream().map(d -> Map.<String,Object>of("despacho",d.get("codigo"),"cliente",d.get("cliente"),
            "unidades",d.get("unidades"),"estado",etiqueta((String)d.get("estado")),"salida",d.get("salida"),"entrega",d.get("entregaEstimada"),"vehiculo",d.get("vehiculo"))).toList();
        return Map.of("tituloFecha","Despachos · " + Fechas.corta(hoy),"resumen",salidas + " salidas previstas hoy · " + unidades + " unidades en ruta",
            "banner","Los lotes quedan reservados al programar el despacho.","kpis",kpis,"despachos",filas,"historial",hist);
    }

    @Transactional(readOnly=true)
    public List<Map<String,Object>> disponibles() {
        return lotes.findAll().stream().filter(l -> "Listo".equals(l.getAreaActual()) && !Set.of("completado","cancelado").contains(l.getOrden().getEstado()))
            .map(l -> {
                long disponible=l.getUnidades()-asignaciones.cantidadEnEstados(l.getId(),List.of("programado","transito","en_transito","entregado"));
                return Map.<String,Object>of("id",l.getId(),"codigo",l.getCodigo(),"orden",l.getOrden().getNumero(),"modelo",l.getOrden().getModelo().getNombre(),
                    "unidades",l.getUnidades(),"disponibles",Math.max(0,disponible));
            }).filter(d -> ((Number)d.get("disponibles")).longValue()>0).toList();
    }

    public Map<String,Object> crear(DespachoRequest r,Usuario usuario) {
        // También serializa reintentos del mismo identificador antes de tomar bloqueos de lote.
        jdbc.execute("SELECT pg_advisory_xact_lock(127954, 2)");
        var previa=despachos.findBySolicitudId(r.solicitudId());
        if (previa.isPresent()) {
            Despacho anterior=previa.get(); var partes=asignaciones.findByDespachoCodigo(anterior.getCodigo());
            Map<String,Integer> cantidades=partes.stream().collect(Collectors.toMap(a -> a.getLote().getId(),DespachoLote::getCantidad));
            boolean mismo=Objects.equals(anterior.getCliente(),r.cliente().trim()) && Objects.equals(anterior.getDestino(),r.destino().trim())
                && Objects.equals(anterior.getVehiculo(),r.vehiculo().trim()) && Objects.equals(anterior.getConductor(),r.conductor().trim())
                && Objects.equals(anterior.getFechaSalida(),r.fechaSalida()) && Objects.equals(anterior.getFechaEntregaEstimada(),r.fechaEntregaEstimada())
                && r.lotes().size()==cantidades.size() && r.lotes().stream().allMatch(a -> Objects.equals(cantidades.get(a.loteId()),a.cantidad()));
            if (!mismo) throw error("SOLICITUD_CAMBIADA","La solicitud ya fue procesada con otros datos");
            return dto(anterior,partes);
        }
        validarFechas(r,true);
        List<Lote> seleccion=bloquearLotes(r);
        validarCantidades(r,seleccion);
        Despacho d=new Despacho(); d.setSolicitudId(r.solicitudId());
        d.setCodigo("DS-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase(Locale.ROOT));
        d.setEstado("programado"); completar(d,r); despachos.saveAndFlush(d);
        var guardadas=guardarAsignaciones(d,r,seleccion,usuario);
        return dto(d,guardadas);
    }

    public Map<String,Object> editar(String codigo,DespachoRequest r,Usuario usuario) {
        Despacho d=buscar(codigo);
        var anteriores=asignaciones.findByDespachoCodigo(codigo);
        if (!"programado".equals(estado(d)) && !("transito".equals(estado(d)) && anteriores.isEmpty()))
            throw error("DESPACHO_CERRADO","Solo se editan despachos programados; un despacho histórico en tránsito puede asociar sus lotes una vez");
        validarFechas(r,false);
        // Bloqueo de todos los lotes, incluidos los liberados, en el mismo orden.
        var ids=new TreeSet<String>(); r.lotes().forEach(a -> ids.add(a.loteId())); anteriores.forEach(a -> ids.add(a.getLote().getId()));
        ids.forEach(id -> lotes.findById(id).orElseThrow(() -> error("LOTE_NO_ENCONTRADO","El lote no existe")));
        asignaciones.deleteAll(anteriores); asignaciones.flush();
        List<Lote> seleccion=bloquearLotes(r); validarCantidades(r,seleccion); completar(d,r); despachos.save(d);
        return dto(d,guardarAsignaciones(d,r,seleccion,usuario));
    }

    public Map<String,Object> actualizarEstado(String codigo,String nuevoEstado,Usuario usuario) {
        Despacho d=buscar(codigo); String actual=estado(d);
        if (nuevoEstado==null || !Set.of("programado","transito","entregado","cancelado").contains(nuevoEstado))
            throw error("ESTADO_INVALIDO","Estado de despacho inválido");
        var partes=asignaciones.findByDespachoCodigo(codigo);
        if (actual.equals(nuevoEstado)) return dto(d,partes);
        boolean permitido=("programado".equals(actual) && Set.of("transito","cancelado").contains(nuevoEstado))
            || ("transito".equals(actual) && "entregado".equals(nuevoEstado));
        if (!permitido) throw error("TRANSICION_INVALIDA","Solo se permite Programado → En tránsito → Entregado o cancelar antes de salir");
        if (!"cancelado".equals(nuevoEstado) && partes.isEmpty()) throw error("DESPACHO_SIN_LOTES","Asocia lotes al despacho antes de continuar");
        partes.stream().map(a -> a.getLote().getId()).distinct().sorted().forEach(id -> lotes.findById(id).orElseThrow());
        if ("transito".equals(nuevoEstado) && d.getFechaSalida().isAfter(LocalDate.now()))
            throw error("SALIDA_FUTURA","No se puede iniciar una salida programada para una fecha futura");
        d.setEstado(nuevoEstado); if ("entregado".equals(nuevoEstado)) d.setEntregadoEn(LocalDateTime.now());
        despachos.saveAndFlush(d);
        for (DespachoLote a:partes) {
            Orden o=a.getLote().getOrden(); String anterior=o.getEstado();
            if ("entregado".equals(nuevoEstado)) {
                long entregado=asignaciones.cantidadEnEstados(a.getLote().getId(),List.of("entregado"));
                if (entregado>=a.getLote().getUnidades()) o.setEstado("completado");
            }
            historial.registrar(o,anterior,usuario,"Despacho " + codigo + ": " + etiqueta(nuevoEstado) + " · " + a.getCantidad() + " unidades");
        }
        return dto(d,partes);
    }

    private Despacho buscar(String codigo) { return despachos.findByCodigo(codigo).orElseThrow(() -> error("DESPACHO_NO_ENCONTRADO","El despacho no existe")); }
    private List<Lote> bloquearLotes(DespachoRequest r) {
        var ids=r.lotes().stream().map(DespachoRequest.Asignacion::loteId).sorted().toList();
        if (new HashSet<>(ids).size()!=ids.size()) throw error("LOTE_REPETIDO","Agrupa la cantidad de cada lote en una sola fila");
        return ids.stream().map(id -> lotes.findById(id).orElseThrow(() -> error("LOTE_NO_ENCONTRADO","El lote no existe"))).toList();
    }
    private void validarCantidades(DespachoRequest r,List<Lote> seleccion) {
        for (Lote l:seleccion) {
            if (!"Listo".equals(l.getAreaActual()) || Set.of("completado","cancelado").contains(l.getOrden().getEstado()))
                throw error("LOTE_NO_DISPONIBLE","Solo se despachan lotes aprobados y pendientes de entrega");
            int cantidad=r.lotes().stream().filter(a -> a.loteId().equals(l.getId())).findFirst().orElseThrow().cantidad();
            long reservado=asignaciones.cantidadEnEstados(l.getId(),List.of("programado","transito","en_transito","entregado"));
            if (cantidad<1 || cantidad>l.getUnidades()-reservado) throw error("CANTIDAD_NO_DISPONIBLE","Saldo disponible de " + l.getCodigo() + ": " + Math.max(0,l.getUnidades()-reservado) + " unidades");
        }
    }
    private List<DespachoLote> guardarAsignaciones(Despacho d,DespachoRequest r,List<Lote> seleccion,Usuario usuario) {
        List<DespachoLote> nuevas=new ArrayList<>();
        for (Lote l:seleccion) {
            DespachoLote a=new DespachoLote(); a.setDespacho(d); a.setLote(l);
            a.setCantidad(r.lotes().stream().filter(x -> x.loteId().equals(l.getId())).findFirst().orElseThrow().cantidad()); nuevas.add(a);
            historial.registrar(l.getOrden(),l.getOrden().getEstado(),usuario,"Reservadas " + a.getCantidad() + " unidades en " + d.getCodigo());
        }
        return asignaciones.saveAll(nuevas);
    }
    private void completar(Despacho d,DespachoRequest r) {
        d.setCliente(r.cliente().trim()); d.setDestino(r.destino().trim()); d.setVehiculo(r.vehiculo().trim()); d.setConductor(r.conductor().trim());
        d.setFechaSalida(r.fechaSalida()); d.setFechaEntregaEstimada(r.fechaEntregaEstimada());
        d.setUnidades(Math.toIntExact(r.lotes().stream().mapToLong(DespachoRequest.Asignacion::cantidad).sum()));
    }
    private void validarFechas(DespachoRequest r,boolean nuevo) {
        if (r.fechaEntregaEstimada().isBefore(r.fechaSalida()) || (nuevo && r.fechaSalida().isBefore(LocalDate.now())))
            throw error("FECHAS_INVALIDAS","La salida no puede ser pasada y la entrega debe ser igual o posterior a la salida");
    }
    private String estado(Despacho d) { return "en_transito".equals(d.getEstado())?"transito":d.getEstado(); }
    private String etiqueta(String estado) { return switch(estado) { case "transito" -> "En tránsito"; case "entregado" -> "Entregado"; case "cancelado" -> "Cancelado"; default -> "Programado"; }; }
    private Map<String,Object> dto(Despacho d,List<DespachoLote> partes) {
        Map<String,Object> m=new LinkedHashMap<>(); m.put("codigo",d.getCodigo()); m.put("solicitudId",d.getSolicitudId());
        m.put("cliente",d.getCliente()); m.put("destino",d.getDestino()); m.put("unidades",d.getUnidades());
        m.put("vehiculo",Objects.requireNonNullElse(d.getVehiculo(),"Sin asignar")); m.put("conductor",Objects.requireNonNullElse(d.getConductor(),"Sin asignar"));
        m.put("estado",estado(d)); m.put("fecha_salida",d.getFechaSalida()); m.put("fecha_entrega_estimada",d.getFechaEntregaEstimada());
        m.put("salida",Fechas.corta(d.getFechaSalida())); m.put("entregaEstimada",Fechas.corta(d.getFechaEntregaEstimada())); m.put("entregadoEn",d.getEntregadoEn());
        m.put("lote",partes.isEmpty()?"Sin asociación registrada":partes.stream().map(a -> a.getLote().getCodigo()+" ("+a.getCantidad()+")").collect(Collectors.joining(", ")));
        m.put("lotes",partes.stream().map(a -> Map.<String,Object>of("loteId",a.getLote().getId(),"codigo",a.getLote().getCodigo(),
            "modelo",a.getLote().getOrden().getModelo().getNombre(),"cantidad",a.getCantidad(),"unidades",a.getLote().getUnidades())).toList());
        return m;
    }
    private BusinessException error(String codigo,String mensaje) { return new BusinessException(codigo,mensaje); }
}
