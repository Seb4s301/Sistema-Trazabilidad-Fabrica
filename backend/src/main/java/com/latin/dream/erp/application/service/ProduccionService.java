package com.latin.dream.erp.application.service;

import com.latin.dream.erp.crosscutting.exception.BusinessException;
import com.latin.dream.erp.domain.entity.Lote;
import com.latin.dream.erp.domain.entity.LoteAvance;
import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.domain.repository.LoteAvanceRepository;
import com.latin.dream.erp.domain.repository.LoteRepository;
import com.latin.dream.erp.domain.repository.UsuarioRepository;
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
    private final UsuarioRepository usuarioRepository;

    public ProduccionService(LoteRepository loteRepository,
                             LoteAvanceRepository avanceRepository,
                             UsuarioRepository usuarioRepository) {
        this.loteRepository = loteRepository;
        this.avanceRepository = avanceRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getProduccionData() {
        log.info("Obteniendo tablero de producción y métricas de planta...");
        List<Lote> todos = loteRepository.findAll();

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
                listasParaDespacho += (l.getUnidades() != null ? l.getUnidades() : 0);
            }

            boolean retrasado = Boolean.TRUE.equals(l.getRetrasado()) ||
                (l.getFechaEntrega() != null && l.getFechaEntrega().isBefore(hoy) && !esListo);
            if (retrasado) {
                lotesConRetraso++;
            }

            Map<String, Object> loteDto = mapLote(l, hoy, retrasado);
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
            Map.of("etiqueta", "Listas para despacho", "valor", String.valueOf(listasParaDespacho))
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
        result.put("totalUnidades", unidadesEnPlanta + listasParaDespacho);

        return result;
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> getLotes() {
        LocalDate hoy = LocalDate.now();
        return loteRepository.findAll().stream()
            .map(l -> mapLote(l, hoy, Boolean.TRUE.equals(l.getRetrasado())))
            .toList();
    }

    public Map<String, Object> registrarAvance(Map<String, Object> payload, Usuario usuario) {
        String loteId = (String) payload.get("lote_id");
        String destino = (String) payload.get("area_destino");
        String notas = (String) payload.get("notas");
        String resultado = (String) payload.get("resultado");

        log.info("Registrando avance de lote {} hacia etapa {}", loteId, destino);

        Lote lote = loteRepository.findById(loteId)
            .orElseGet(() -> loteRepository.findByCodigo(loteId)
                .orElseThrow(() -> new BusinessException("LOTE_NO_ENCONTRADO", "No se encontró el lote " + loteId)));

        String origen = lote.getAreaActual();
        lote.setAreaActual(StringUtils.capitalize(destino));
        loteRepository.save(lote);

        LoteAvance avance = LoteAvance.builder()
            .lote(lote)
            .areaOrigen(origen)
            .areaDestino(destino)
            .responsable(usuario != null ? usuario : lote.getOrden().getRegistradoPor())
            .inicio(LocalDateTime.now())
            .resultado(resultado)
            .notas(notas)
            .build();
        avanceRepository.save(avance);

        log.info("Avance registrado con éxito para lote {}", lote.getCodigo());
        return mapLote(lote, LocalDate.now(), Boolean.TRUE.equals(lote.getRetrasado()));
    }

    private Map<String, Object> mapLote(Lote l, LocalDate hoy, boolean retrasado) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", l.getId());
        m.put("codigo", l.getCodigo());
        m.put("orden", l.getOrden() != null ? l.getOrden().getNumero() : "—");
        m.put("modelo", l.getOrden() != null && l.getOrden().getModelo() != null ? l.getOrden().getModelo().getNombre() : "Colchón");
        m.put("plaza", l.getOrden() != null && l.getOrden().getModelo() != null ? l.getOrden().getModelo().getCategoria() : "Queen");
        m.put("unidades", l.getUnidades() != null ? l.getUnidades() : 0);
        m.put("area_actual", l.getAreaActual());
        m.put("prioridad", l.getOrden() != null ? l.getOrden().getPrioridad() : "Normal");

        String resp = "Operador Planta";
        if (l.getOrden() != null && l.getOrden().getRegistradoPor() != null) {
            resp = l.getOrden().getRegistradoPor().getNombre();
        }
        m.put("responsable", resp);

        String[] parts = StringUtils.split(resp, " ");
        String iniciales = (parts != null && parts.length >= 2)
            ? ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase()
            : StringUtils.upperCase(StringUtils.substring(resp, 0, 1));
        m.put("iniciales", iniciales);

        if (l.getFechaEntrega() != null) {
            String[] meses = {"ene.", "feb.", "mar.", "abr.", "may.", "jun.", "jul.", "ago.", "set.", "oct.", "nov.", "dic."};
            int mes = l.getFechaEntrega().getMonthValue();
            m.put("entrega", l.getFechaEntrega().getDayOfMonth() + " " + meses[mes - 1] + " " + l.getFechaEntrega().getYear());
            m.put("fecha_entrega", l.getFechaEntrega().toString());
            if (l.getFechaEntrega().isBefore(hoy)) {
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
