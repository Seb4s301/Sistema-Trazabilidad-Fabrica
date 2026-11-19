package com.latin.dream.erp.application.service;
import com.latin.dream.erp.application.dto.*;
import com.latin.dream.erp.domain.entity.*;
import com.latin.dream.erp.domain.repository.*;
import com.latin.dream.erp.crosscutting.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
@Service @Transactional
public class CatalogoGestionService {
    private final ModeloRepository modelos; private final PrecioComisionRepository tarifas; private final OrdenRepository ordenes;
    public CatalogoGestionService(ModeloRepository modelos,PrecioComisionRepository tarifas,OrdenRepository ordenes) { this.modelos=modelos; this.tarifas=tarifas; this.ordenes=ordenes; }
    @Transactional(readOnly=true) public Map<String,Object> listar() {
        return Map.of("modelos",modelos.findAll().stream().map(this::modeloDto).toList(),"tarifas",tarifas.findAll().stream().map(this::tarifaDto).toList());
    }
    public Map<String,Object> modelo(String id,ModeloRequest r) {
        Modelo m=id==null?new Modelo():modelos.bloquearPorId(id).orElseThrow(() -> error("MODELO_NO_ENCONTRADO","El modelo no existe"));
        if (modelos.existsByCodigoAndIdNot(r.codigo().trim(),id==null?"":id)) throw error("CODIGO_DUPLICADO","El código de modelo ya existe");
        if (id!=null && ordenes.existsByModeloId(id) && (!Objects.equals(m.getCodigo(),r.codigo().trim()) || !Objects.equals(m.getNombre(),r.nombre().trim())
            || !Objects.equals(m.getCategoria(),r.categoria().trim()) || !Objects.equals(m.getDimensiones(),r.dimensiones().trim()) || !Objects.equals(m.getRelleno(),r.relleno().trim())))
            throw error("MODELO_EN_USO","Este modelo ya tiene órdenes; crea otro modelo para cambiar sus especificaciones. Puedes activar o desactivar el actual");
        m.setCodigo(r.codigo().trim()); m.setNombre(r.nombre().trim()); m.setCategoria(r.categoria().trim()); m.setDimensiones(r.dimensiones().trim());
        m.setRelleno(r.relleno().trim()); m.setActivo(r.activo()); return modeloDto(modelos.saveAndFlush(m));
    }
    public Map<String,Object> tarifa(String id,TarifaRequest r) {
        Modelo m=modelos.bloquearPorId(r.modeloId()).orElseThrow(() -> error("MODELO_NO_ENCONTRADO","El modelo no existe"));
        if (r.vigenteHasta()!=null && r.vigenteHasta().isBefore(r.vigenteDesde())) throw error("VIGENCIA_INVALIDA","La vigencia final no puede ser anterior al inicio");
        PrecioComision t=id==null?new PrecioComision():tarifas.findById(id).orElseThrow(() -> error("TARIFA_NO_ENCONTRADA","La tarifa no existe"));
        if (id!=null && !t.getModelo().getId().equals(r.modeloId())) throw error("MODELO_TARIFA_INVALIDO","No se cambia el modelo de una tarifa existente");
        for (PrecioComision otra:tarifas.findAll()) {
            if (!otra.getModelo().getId().equals(r.modeloId()) || Objects.equals(otra.getId(),id)) continue;
            boolean cruza=(otra.getVigenteHasta()==null || !otra.getVigenteHasta().isBefore(r.vigenteDesde())) && (r.vigenteHasta()==null || !r.vigenteHasta().isBefore(otra.getVigenteDesde()));
            if (!cruza) continue;
            if (id==null && otra.getVigenteHasta()==null && otra.getVigenteDesde().isBefore(r.vigenteDesde())) {
                otra.setVigenteHasta(r.vigenteDesde().minusDays(1)); tarifas.save(otra);
            } else throw error("VIGENCIAS_SOLAPADAS","La tarifa se solapa con otra del mismo modelo");
        }
        t.setModelo(m); t.setCategoria(m.getCategoria()); t.setPrecioBase(r.precioBase()); t.setPorcentaje(r.porcentaje());
        t.setVigenteDesde(r.vigenteDesde()); t.setVigenteHasta(r.vigenteHasta()); return tarifaDto(tarifas.saveAndFlush(t));
    }
    private Map<String,Object> modeloDto(Modelo m) { return Map.of("id",m.getId(),"codigo",m.getCodigo(),"nombre",m.getNombre(),"categoria",m.getCategoria(),"dimensiones",m.getDimensiones(),"relleno",m.getRelleno(),"activo",m.getActivo(),"usado",ordenes.existsByModeloId(m.getId())); }
    private Map<String,Object> tarifaDto(PrecioComision t) { Map<String,Object> d=new LinkedHashMap<>(); d.put("id",t.getId()); d.put("modeloId",t.getModelo().getId()); d.put("modelo",t.getModelo().getNombre()); d.put("precioBase",t.getPrecioBase()); d.put("porcentaje",t.getPorcentaje()); d.put("vigenteDesde",t.getVigenteDesde()); d.put("vigenteHasta",t.getVigenteHasta()); return d; }
    private BusinessException error(String c,String m) { return new BusinessException(c,m); }
}
