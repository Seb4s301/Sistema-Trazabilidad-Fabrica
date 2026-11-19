package com.latin.dream.erp.application.service;

import com.latin.dream.erp.application.dto.OrdenRequest;
import com.latin.dream.erp.application.dto.OrdenResponse;
import com.latin.dream.erp.domain.entity.Orden;
import com.latin.dream.erp.domain.entity.Modelo;
import com.latin.dream.erp.domain.entity.Usuario;
import com.latin.dream.erp.domain.entity.Lote;
import com.latin.dream.erp.domain.repository.OrdenRepository;
import com.latin.dream.erp.domain.repository.ModeloRepository;
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
    private final LoteRepository loteRepository;
    private final NumeroOrdenService numeroOrdenService;
    private final HistorialService historial;

    public OrdenService(OrdenRepository ordenRepository,
                        ModeloRepository modeloRepository,
                        LoteRepository loteRepository,
                        NumeroOrdenService numeroOrdenService, HistorialService historial) {
        this.ordenRepository = ordenRepository;
        this.modeloRepository = modeloRepository;
        this.loteRepository = loteRepository;
        this.numeroOrdenService = numeroOrdenService;
        this.historial = historial;
    }

    @Transactional(readOnly = true)
    public List<OrdenResponse> listar(String estado, String prioridad) {
        java.util.Map<String, Lote> codigos = loteRepository.findAll().stream()
            .collect(Collectors.toMap(l -> l.getOrden().getId(), l -> l, (a, b) -> a));
        return ordenRepository.findAll().stream()
            .filter(o -> estado == null || estado.isBlank() || o.getEstado().equalsIgnoreCase(estado))
            .filter(o -> prioridad == null || prioridad.isBlank() || prioridad.equalsIgnoreCase(o.getPrioridad()))
            .map(o -> toResponse(o, codigos.get(o.getId())))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public OrdenResponse buscarPorNumero(String numero) {
        Orden orden = ordenRepository.findByNumero(numero)
            .orElseThrow(() -> new BusinessException("ORDEN_NO_ENCONTRADA", "No se encontró la orden " + numero));
        return toResponse(orden);
    }

    public OrdenResponse guardarBorrador(OrdenRequest request, Usuario usuario) {
        Modelo modelo = obtenerModelo(request.getModeloId());
        Orden orden = Orden.builder()
            // Identificador temporal de 20 caracteres.
            .numero("BORR-" + java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 15))
            .modelo(modelo)
            .cantidad(request.getCantidad())
            .prioridad(request.getPrioridad())
            .fechaEntrega(request.getFechaEntrega())
            .observaciones(request.getObservaciones())
            .estado("borrador")
            .registradoPor(usuario)
            .turno(usuario != null ? usuario.getTurno() : "Turno A")
            .build();

        Orden guardada = ordenRepository.save(orden);
        historial.registrar(guardada, null, usuario, "Borrador guardado");
        return toResponse(guardada);
    }

    public OrdenResponse crearOrden(String id, Usuario usuario) {
        numeroOrdenService.bloquearNumeracion();
        Orden orden = ordenRepository.bloquearPorId(id)
            .orElseThrow(() -> new BusinessException("ORDEN_NO_ENCONTRADA", "La orden no existe"));

        if (!"borrador".equalsIgnoreCase(orden.getEstado())) {
            throw new BusinessException("ORDEN_NO_ES_BORRADOR", "Solo se pueden crear órdenes desde estado borrador");
        }

        validarCompleta(orden);
        if (orden.getFechaEntrega().isBefore(LocalDate.now())) {
            throw new BusinessException("FECHA_ENTREGA_PASADA", "La fecha de entrega no puede ser anterior a hoy");
        }

        String numeroOrden = numeroOrdenService.generarSiguiente();
        orden.setNumero(numeroOrden);
        orden.setEstado("pendiente_corte");

        Orden guardada = ordenRepository.save(orden);

        // Generar lote asociado (regla: un lote por orden)
        String anio2 = String.valueOf(java.time.Year.now().getValue()).substring(2);
        String mes = String.format("%02d", java.time.LocalDate.now().getMonthValue());
        String secuencia = numeroOrden.substring(8);
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
        historial.registrar(guardada, "borrador", usuario, "Orden confirmada e ingresada a Corte");

        return toResponse(guardada);
    }

    public OrdenResponse crear(OrdenRequest request, Usuario usuario) {
        numeroOrdenService.bloquearNumeracion();
        if (request.getSolicitudId() == null) throw new BusinessException("SOLICITUD_REQUERIDA", "Falta el identificador de solicitud");
        var previa = ordenRepository.findBySolicitudId(request.getSolicitudId());
        if (previa.isPresent()) {
            Orden o=previa.get();
            if (o.getRegistradoPor()==null || usuario==null || !o.getRegistradoPor().getId().equals(usuario.getId()))
                throw new BusinessException("SOLICITUD_INVALIDA", "La solicitud pertenece a otro usuario");
            if (!java.util.Objects.equals(o.getCantidad(),request.getCantidad()) || o.getModelo()==null
                    || !o.getModelo().getId().equals(request.getModeloId()) || !java.util.Objects.equals(o.getFechaEntrega(),request.getFechaEntrega())
                    || !java.util.Objects.equals(o.getPrioridad(),request.getPrioridad()) || !java.util.Objects.equals(o.getObservaciones(),request.getObservaciones()))
                throw new BusinessException("SOLICITUD_CAMBIADA", "La solicitud ya fue procesada con otros datos");
            return toResponse(o);
        }
        OrdenResponse borrador=guardarBorrador(request,usuario);
        Orden o=ordenRepository.bloquearPorId(borrador.getId()).orElseThrow();
        o.setSolicitudId(request.getSolicitudId());
        return crearOrden(o.getId(),usuario);
    }

    public OrdenResponse editarBorrador(String id, OrdenRequest request, Usuario usuario) {
        Orden o=ordenRepository.bloquearPorId(id).orElseThrow(() -> new BusinessException("ORDEN_NO_ENCONTRADA","La orden no existe"));
        exigirBorrador(o);
        o.setModelo(obtenerModelo(request.getModeloId())); o.setCantidad(request.getCantidad()); o.setPrioridad(request.getPrioridad());
        o.setFechaEntrega(request.getFechaEntrega()); o.setObservaciones(request.getObservaciones());
        ordenRepository.save(o); historial.registrar(o,"borrador",usuario,"Borrador actualizado"); return toResponse(o);
    }

    public void eliminarBorrador(String id) {
        Orden o=ordenRepository.bloquearPorId(id).orElseThrow(() -> new BusinessException("ORDEN_NO_ENCONTRADA","La orden no existe"));
        exigirBorrador(o); historial.borrarBorrador(id); ordenRepository.delete(o);
    }

    private void exigirBorrador(Orden o) {
        if (!"borrador".equals(o.getEstado())) throw new BusinessException("ORDEN_NO_ES_BORRADOR","Solo se pueden editar o eliminar borradores");
    }
    private Modelo obtenerModelo(String id) {
        if (id==null || id.isBlank()) return null;
        Modelo m=modeloRepository.findById(id).orElseThrow(() -> new BusinessException("MODELO_NO_ENCONTRADO","El modelo no existe"));
        if (!Boolean.TRUE.equals(m.getActivo())) throw new BusinessException("MODELO_INACTIVO","El modelo está inactivo");
        return m;
    }
    private void validarCompleta(Orden o) {
        if (o.getModelo()==null || !Boolean.TRUE.equals(o.getModelo().getActivo()) || o.getCantidad()==null || o.getCantidad()<1
            || o.getFechaEntrega()==null || o.getPrioridad()==null || !java.util.Set.of("Alta","Normal","Baja").contains(o.getPrioridad()))
            throw new BusinessException("ORDEN_INCOMPLETA","Completa modelo activo, cantidad, prioridad y fecha antes de confirmar");
    }

    private OrdenResponse toResponse(Orden orden) {
        Lote lote = loteRepository.findByOrdenNumero(orden.getNumero()).stream().findFirst().orElse(null);
        return toResponse(orden, lote);
    }

    private OrdenResponse toResponse(Orden orden, Lote lote) {
        OrdenResponse dto = new OrdenResponse();
        dto.setId(orden.getId());
        dto.setModeloId(orden.getModelo()==null?null:orden.getModelo().getId());
        dto.setObservaciones(orden.getObservaciones());
        dto.setNumero(orden.getNumero());
        String mNombre = orden.getModelo() != null ? orden.getModelo().getNombre() : null;
        dto.setModelo(mNombre);
        dto.setModeloNombre(mNombre);
        dto.setModeloCodigo(orden.getModelo() != null ? orden.getModelo().getCodigo() : null);
        dto.setCategoria(orden.getModelo() != null ? orden.getModelo().getCategoria() : null);
        dto.setCantidad(orden.getCantidad());
        dto.setPrioridad(orden.getPrioridad());
        dto.setFechaEntrega(orden.getFechaEntrega());
        dto.setEntrega(Fechas.corta(orden.getFechaEntrega()));
        dto.setEstado(orden.getEstado());
        dto.setEtapaInicial("pendiente_corte".equalsIgnoreCase(orden.getEstado()) ? "Pendiente de corte" : orden.getEstado());
        dto.setRegistradoPor(orden.getRegistradoPor() != null ? orden.getRegistradoPor().getNombre() : null);
        dto.setTurno(orden.getTurno());

        dto.setLote(lote==null?"—":lote.getCodigo());
        dto.setLoteId(lote==null?null:lote.getId());

        return dto;
    }
}
