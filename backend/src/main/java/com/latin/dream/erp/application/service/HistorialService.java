package com.latin.dream.erp.application.service;
import com.latin.dream.erp.domain.entity.*;
import com.latin.dream.erp.domain.repository.HistorialOrdenRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.*;
@Service @Transactional
public class HistorialService {
    private final HistorialOrdenRepository repository;
    public HistorialService(HistorialOrdenRepository repository) { this.repository=repository; }
    public void registrar(Orden orden, String anterior, Usuario usuario, String notas) {
        HistorialOrden h=new HistorialOrden(); h.setOrden(orden); h.setEstadoAnterior(anterior);
        h.setEstadoNuevo(orden.getEstado()); h.setUsuario(usuario); h.setFecha(LocalDateTime.now()); h.setNotas(notas);
        repository.save(h);
    }
    @Transactional(readOnly=true)
    public List<Map<String,Object>> consultar(String id) {
        return repository.findByOrdenIdOrderByFechaDesc(id).stream().map(h -> {
            Map<String,Object> d=new LinkedHashMap<>(); d.put("fecha",h.getFecha()); d.put("anterior",h.getEstadoAnterior());
            d.put("estado",h.getEstadoNuevo()); d.put("notas",h.getNotas());
            d.put("usuario",h.getUsuario()==null?"Sin usuario":h.getUsuario().getNombre()); return d;
        }).toList();
    }
    public void borrarBorrador(String id) { repository.deleteByOrdenId(id); }
}
