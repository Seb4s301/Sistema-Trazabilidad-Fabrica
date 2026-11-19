package com.latin.dream.erp.application.service;
import com.latin.dream.erp.domain.entity.Lote;
import com.latin.dream.erp.domain.repository.DespachoLoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @Transactional(readOnly=true)
public class SaldosLoteService {
    public record Saldo(int disponibles,int reservadas,int entregadas,int pendientes) {}
    private final DespachoLoteRepository asignaciones;
    public SaldosLoteService(DespachoLoteRepository asignaciones) { this.asignaciones=asignaciones; }
    public Map<String,Saldo> calcular(List<Lote> lotes) {
        Map<String,Integer> entregadas=new HashMap<>(),reservadas=new HashMap<>();
        for (var a:asignaciones.findAll()) {
            String estado=a.getDespacho().getEstado();
            if ("entregado".equals(estado)) entregadas.merge(a.getLote().getId(),a.getCantidad(),Integer::sum);
            else if (Set.of("programado","transito","en_transito").contains(estado)) reservadas.merge(a.getLote().getId(),a.getCantidad(),Integer::sum);
        }
        Map<String,Saldo> resultado=new HashMap<>();
        for (Lote l:lotes) {
            int e=entregadas.getOrDefault(l.getId(),0),r=reservadas.getOrDefault(l.getId(),0),total=l.getUnidades()==null?0:l.getUnidades();
            resultado.put(l.getId(),new Saldo(Math.max(0,total-e-r),r,e,Math.max(0,total-e)));
        }
        return resultado;
    }
}
