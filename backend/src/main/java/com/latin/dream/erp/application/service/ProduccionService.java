package com.latin.dream.erp.application.service;

import com.latin.dream.erp.crosscutting.exception.BusinessException;
import com.latin.dream.erp.domain.entity.Lote;
import com.latin.dream.erp.domain.entity.LoteAvance;
import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.domain.repository.LoteAvanceRepository;
import com.latin.dream.erp.domain.repository.LoteRepository;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Servicio de Aplicación: Casos de uso de Producción y Tablero de Trazabilidad.
 * Maneja lotes, áreas de planta y registro de avances de etapa.
 */
@Service
@Transactional
public class ProduccionService {

    private static final Logger log = LoggerFactory.getLogger(ProduccionService.class);

    private final LoteRepository loteRepository;
    private final LoteAvanceRepository avanceRepository;
    private final HistorialService historial;
    private final SaldosLoteService saldos;

    public ProduccionService(LoteRepository loteRepository,
                             LoteAvanceRepository avanceRepository, HistorialService historial, SaldosLoteService saldos) {
        this.loteRepository = loteRepository;
        this.avanceRepository = avanceRepository;
        this.historial = historial;
        this.saldos=saldos;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProduccionData() {
        log.info("Obteniendo tablero de producción y métricas de planta...");
        List<Lote> todos = loteRepository.findAll().stream()
            .filter(l -> !Set.of("completado","cancelado").contains(l.getOrden().getEstado())).toList();
        Map<String,String> responsables=ultimosResponsables();
        Map<String,SaldosLoteService.Saldo> balances=saldos.calcular(todos);

        int lotesEnPlanta = 0;
        int unidadesEnPlanta = 0;
        int lotesConRetraso = 0;
        int listasParaDespacho = 0;

        LocalDate hoy = LocalDate.now();

        // 5 áreas canónicas
        String[] areaIds = {"corte", "ensamblaje", "tapizado", "calidad", "listo"};
        String[] areaNombres = {"Corte", "Ensamblaje", "Tapizado", "Calidad", "Listo"};
        String[] areaDesc = {
            "Corte de telas y espumas",
            "Núcleo y estructura",
            "Costura y tapizado final",
            "Inspección y control",
            "Alistados para despacho"
        };

        Map<String, List<Map<String, Object>>> lotesPorArea = new LinkedHashMap<>();
        for (String id : areaIds) {
            lotesPorArea.put(id, new ArrayList<>());
        }

        for (Lote l : todos) {
            boolean esListo = StringUtils.equalsIgnoreCase("listo", l.getAreaActual());
            if (!esListo) {
                lotesEnPlanta++;
                unidadesEnPlanta += (l.getUnidades() != null ? l.getUnidades() : 0);
            } else {
                listasParaDespacho += balances.get(l.getId()).disponibles();
            }

            boolean retrasado = (l.getFechaEntrega() != null && l.getFechaEntrega().isBefore(hoy) && !esListo);
            if (retrasado) {
                lotesConRetraso++;
            }

            Map<String, Object> loteDto = mapLote(l, hoy, retrasado, responsables, balances.get(l.getId()));
            String areaKey = StringUtils.lowerCase(StringUtils.defaultIfBlank(l.getAreaActual(), "corte"));
            if (lotesPorArea.containsKey(areaKey)) {
                lotesPorArea.get(areaKey).add(loteDto);
            } else {
                lotesPorArea.get("corte").add(loteDto);
            }
        }

        List<Map<String, Object>> metricas = List.of(
            Map.of("etiqueta", "Lotes en planta", "valor", String.valueOf(lotesEnPlanta)),
            Map.of("etiqueta", "Unidades en planta", "valor", String.valueOf(unidadesEnPlanta)),
            Map.of("etiqueta", "Lotes con retraso", "valor", String.valueOf(lotesConRetraso), "peligro", lotesConRetraso > 0),
            Map.of("etiqueta", "Unidades disponibles para despacho", "valor", String.valueOf(listasParaDespacho))
        );

        List<Map<String, Object>> areasList = new ArrayList<>();
        for (int i = 0; i < areaIds.length; i++) {
            String aid = areaIds[i];
            List<Map<String, Object>> lotes = lotesPorArea.get(aid);
            int unidadesArea = lotes.stream().mapToInt(item -> (int) item.get("unidades")).sum();

            Map<String, Object> areaObj = new LinkedHashMap<>();
            areaObj.put("id", aid);
            areaObj.put("nombre", areaNombres[i]);
            areaObj.put("subtitulo", areaDesc[i] + " · " + unidadesArea + " unidades");
            areaObj.put("lotes", lotes);
            areasList.add(areaObj);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("metricas", metricas);
        result.put("areas", areasList);
        result.put("totalLotes", todos.size());
        result.put("totalUnidades", todos.stream().mapToInt(l -> balances.get(l.getId()).pendientes()).sum());

        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLotes() {
        LocalDate hoy = LocalDate.now();
        Map<String,String> responsables=ultimosResponsables();
        List<Lote> lista=loteRepository.findAll();
        var balances=saldos.calcular(lista);
        return lista.stream()
            .map(l -> mapLote(l, hoy, (l.getFechaEntrega() != null && l.getFechaEntrega().isBefore(hoy) && !"Listo".equalsIgnoreCase(l.getAreaActual())), responsables, balances.get(l.getId())))
            .toList();
    }

    public Map<String, Object> registrarAvance(com.latin.dream.erp.application.dto.AvanceRequest payload, Usuario usuario) {
        String loteId = payload.loteId();
        String destino = payload.areaDestino();
        String notas = payload.notas();
        String resultado = payload.resultado();

        log.info("Registrando avance de lote {} hacia etapa {}", loteId, destino);

        Lote lote = loteRepository.findById(loteId)
            .orElseGet(() -> loteRepository.findByCodigo(loteId)
                .orElseThrow(() -> new BusinessException("LOTE_NO_ENCONTRADO", "No se encontró el lote " + loteId)));

        String origen = lote.getAreaActual();
        destino = StringUtils.capitalize(StringUtils.lowerCase(StringUtils.trim(destino)));
        List<String> etapas = List.of("Corte", "Ensamblaje", "Tapizado", "Calidad", "Listo");
        int actual = etapas.indexOf(origen);
        int siguiente = etapas.indexOf(destino);
        boolean retrabajo = "Calidad".equals(origen) && "Tapizado".equals(destino) && "rechazado".equals(resultado);
        if (siguiente < 0 || actual < 0 || (siguiente != actual + 1 && !retrabajo)) {
            throw new BusinessException("ETAPA_INVALIDA", "Avanza a la siguiente etapa o registra el rechazo de Calidad a Tapizado");
        }
        if ("Listo".equals(destino) && !"aprobado".equals(resultado)) {
            throw new BusinessException("CALIDAD_REQUERIDA", "El lote requiere aprobación de Calidad antes del despacho");
        }
        if (StringUtils.isNotBlank(resultado) && !("Calidad".equals(origen)
                && (("Listo".equals(destino) && "aprobado".equals(resultado)) || retrabajo))) {
            throw new BusinessException("RESULTADO_INVALIDO", "El resultado solo corresponde a la salida de Calidad");
        }
        if (Set.of("cancelado", "completado").contains(lote.getOrden().getEstado())) {
            throw new BusinessException("ORDEN_CERRADA", "No se puede avanzar una orden cerrada");
        }
        lote.setAreaActual(destino);
        lote.setRetrasado(!"Listo".equals(destino) && lote.getFechaEntrega().isBefore(LocalDate.now()));
        String estadoAnterior=lote.getOrden().getEstado();
        lote.getOrden().setEstado(switch (destino) {
            case "Corte" -> "pendiente_corte";
            case "Calidad" -> "calidad";
            case "Listo" -> "listo_despacho";
            default -> "en_proceso";
        });
        loteRepository.save(lote);

        LoteAvance avance = LoteAvance.builder()
            .lote(lote)
            .areaOrigen(origen)
            .areaDestino(destino)
            .responsable(usuario != null ? usuario : lote.getOrden().getRegistradoPor())
            .inicio(LocalDateTime.now())
            .fin(LocalDateTime.now())
            .resultado(resultado)
            .notas(notas)
            .build();
        avanceRepository.save(avance);
        historial.registrar(lote.getOrden(),estadoAnterior,usuario,"Avance de " + origen + " a " + destino);

        log.info("Avance registrado con éxito para lote {}", lote.getCodigo());
        return mapLote(lote, LocalDate.now(), (lote.getFechaEntrega().isBefore(LocalDate.now()) && !"Listo".equals(lote.getAreaActual())), Map.of(lote.getId(),StringUtils.defaultIfBlank(usuario.getNombre(),"Operador Planta")), saldos.calcular(List.of(lote)).get(lote.getId()));
    }

    private Map<String,String> ultimosResponsables() {
        Map<String,String> ultimos=new HashMap<>();
        for (LoteAvance a:avanceRepository.findAllByOrderByInicioDesc()) {
            if (a.getResponsable()!=null) ultimos.putIfAbsent(a.getLote().getId(),a.getResponsable().getNombre());
        }
        return ultimos;
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> historialLote(String id) {
        return avanceRepository.findByLoteIdOrderByInicioDesc(id).stream().map(a -> {
            Map<String,Object> d=new LinkedHashMap<>(); d.put("origen",a.getAreaOrigen()); d.put("destino",a.getAreaDestino());
            d.put("fecha",a.getInicio()); d.put("resultado",a.getResultado()); d.put("notas",a.getNotas());
            d.put("responsable",a.getResponsable()==null?"Sin responsable":a.getResponsable().getNombre()); return d;
        }).toList();
    }

    private Map<String, Object> mapLote(Lote l, LocalDate hoy, boolean retrasado, Map<String,String> responsables, SaldosLoteService.Saldo saldo) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("codigo", l.getCodigo());
        m.put("orden", l.getOrden() != null ? l.getOrden().getNumero() : "—");
        m.put("modelo", l.getOrden() != null && l.getOrden().getModelo() != null ? l.getOrden().getModelo().getNombre() : "Colchón");
        m.put("plaza", l.getOrden() != null && l.getOrden().getModelo() != null ? l.getOrden().getModelo().getCategoria() : "Queen");
        m.put("unidades", l.getUnidades() != null ? l.getUnidades() : 0);
        if (saldo!=null) {
            m.put("disponibles",saldo.disponibles()); m.put("reservadas",saldo.reservadas()); m.put("entregadas",saldo.entregadas()); m.put("pendientes",saldo.pendientes());
        }
        m.put("area_actual", l.getAreaActual());
        m.put("prioridad", l.getOrden() != null ? l.getOrden().getPrioridad() : "Normal");

        String resp = "Operador Planta";
        if (l.getOrden() != null && l.getOrden().getRegistradoPor() != null) {
            resp = l.getOrden().getRegistradoPor().getNombre();
        }
        resp=responsables.getOrDefault(l.getId(),resp);
        m.put("responsable", resp);
        m.put("observaciones",l.getOrden().getObservaciones());

        String[] parts = StringUtils.split(resp, " ");
        String iniciales = (parts != null && parts.length >= 2)
            ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
            : StringUtils.upperCase(StringUtils.substring(resp, 0, 1));
        m.put("iniciales", iniciales);

        if (l.getFechaEntrega() != null) {
            m.put("entrega", Fechas.corta(l.getFechaEntrega()));
            m.put("fecha_entrega", l.getFechaEntrega().toString());
            if (retrasado && l.getFechaEntrega().isBefore(hoy)) {
                long dias = ChronoUnit.DAYS.between(l.getFechaEntrega(), hoy);
                m.put("diasRetraso", dias);
            }
        } else {
            m.put("entrega", "—");
        }

        m.put("retrasado", retrasado);
        m.put("aprobado", StringUtils.equalsIgnoreCase("listo", l.getAreaActual()));

        return m;
    }
}
