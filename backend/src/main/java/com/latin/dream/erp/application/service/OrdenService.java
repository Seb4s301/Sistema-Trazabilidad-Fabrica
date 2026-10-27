package com.latin.dream.erp.application.service;

import com.latin.dream.erp.application.dto.OrdenRequest;
import com.latin.dream.erp.application.dto.OrdenResponse;
import com.latin.dream.erp.domain.entity.Orden;
import com.latin.dream.erp.domain.entity.Modelo;
import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.domain.entity.Lote;
import com.latin.dream.erp.domain.repository.OrdenRepository;
import com.latin.dream.erp.domain.repository.ModeloRepository;
import com.latin.dream.erp.domain.repository.UsuarioRepository;
import com.latin.dream.erp.domain.repository.LoteRepository;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio de aplicación: casos de uso de Órdenes.
 * Orquesta el flujo entre dominio e infraestructura.
 */
@Service
@Transactional
public class OrdenService {

    private final OrdenRepository ordenRepository;
    private final ModeloRepository modeloRepository;
    private final UsuarioRepository usuarioRepository;
    private final LoteRepository loteRepository;
    private final NumeroOrdenService numeroOrdenService;

    public OrdenService(OrdenRepository ordenRepository,
                        ModeloRepository modeloRepository,
                        UsuarioRepository usuarioRepository,
                        LoteRepository loteRepository,
                        NumeroOrdenService numeroOrdenService) {
        this.ordenRepository = ordenRepository;
        this.modeloRepository = modeloRepository;
        this.usuarioRepository = usuarioRepository;
        this.loteRepository = loteRepository;
        this.numeroOrdenService = numeroOrdenService;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listar(String estado, String prioridad) {
        return ordenRepository.findAll().stream()
            .filter(o -> estado == null || estado.isBlank() || o.getEstado().equalsIgnoreCase(estado))
            .filter(o -> prioridad == null || prioridad.isBlank() || o.getPrioridad().equalsIgnoreCase(prioridad))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrdenResponse buscarPorNumero(String numero) {
        Orden orden = ordenRepository.findByNumero(numero)
            .orElseThrow(() -> new BusinessException("ORDEN_NO_ENCONTRADA", "No se encontró la orden " + numero));
        return toResponse(orden);
    }

    public OrdenResponse guardarBorrador(OrdenRequest request, Usuario usuario) {
        Modelo modelo = modeloRepository.findById(request.getModeloId())
            .orElseThrow(() -> new BusinessException("MODELO_NO_ENCONTRADO", "El modelo seleccionado no existe"));

        Orden orden = Orden.builder()
            .numero("BORRADOR-" + System.currentTimeMillis())
            .modelo(modelo)
            .cantidad(request.getCantidad())
            .prioridad(request.getPrioridad())
            .fechaEntrega(request.getFechaEntrega())
            .observaciones(request.getObservaciones())
            .estado("borrador")
            .registradoPor(usuario)
            .turno(usuario.getTurno())
            .build();

        Orden guardada = ordenRepository.save(orden);
        return toResponse(guardada);
    }

    public OrdenResponse crearOrden(String id, Usuario usuario) {
        Orden orden = ordenRepository.findById(id)
            .orElseThrow(() -> new BusinessException("ORDEN_NO_ENCONTRADA", "La orden no existe"));

        if (!"borrador".equalsIgnoreCase(orden.getEstado())) {
            throw new BusinessException("ORDEN_NO_ES_BORRADOR", "Solo se pueden crear órdenes desde estado borrador");
        }

        if (orden.getFechaEntrega().isBefore(LocalDate.now())) {
            throw new BusinessException("FECHA_ENTREGA_PASADA", "La fecha de entrega no puede ser anterior a hoy");
        }

        String numeroOrden = numeroOrdenService.generarSiguiente();
        orden.setNumero(numeroOrden);
        orden.setEstado("pendiente_corte");
        orden.setRegistradoPor(usuario);

        Orden guardada = ordenRepository.save(orden);

        // Generar lote asociado (regla: un lote por orden)
        // LT-AAMM-NNNN: AA = año (2 dígitos), MM = mes de creación, NNNN = secuencia de la orden
        String anio2 = String.valueOf(java.time.Year.now().getValue()).substring(2);
        String mes = String.format("%02d", java.time.LocalDate.now().getMonthValue());
        String secuencia = numeroOrden.substring(numeroOrden.length() - 4);
        String codigoLote = "LT-" + anio2 + mes + "-" + secuencia;

        Lote lote = Lote.builder()
            .codigo(codigoLote)
            .orden(guardada)
            .unidades(guardada.getCantidad())
            .areaActual("Corte")
            .fechaEntrega(guardada.getFechaEntrega())
            .retrasado(false)
            .build();
        loteRepository.save(lote);

        return toResponse(guardada);
    }

    private OrdenResponse toResponse(Orden orden) {
        OrdenResponse dto = new OrdenResponse();
        dto.setId(orden.getId());
        dto.setNumero(orden.getNumero());
        dto.setModeloNombre(orden.getModelo() != null ? orden.getModelo().getNombre() : null);
        dto.setModeloCodigo(orden.getModelo() != null ? orden.getModelo().getCodigo() : null);
        dto.setCantidad(orden.getCantidad());
        dto.setPrioridad(orden.getPrioridad());
        dto.setFechaEntrega(orden.getFechaEntrega());
        dto.setEstado(orden.getEstado());
        dto.setEtapaInicial("pendiente_corte".equalsIgnoreCase(orden.getEstado()) ? "Pendiente de corte" : orden.getEstado());
        dto.setRegistradoPor(orden.getRegistradoPor() != null ? orden.getRegistradoPor().getNombre() : null);
        dto.setTurno(orden.getTurno());
        return dto;
    }
}
