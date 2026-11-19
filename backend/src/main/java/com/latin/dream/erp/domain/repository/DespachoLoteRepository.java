package com.latin.dream.erp.domain.repository;
import com.latin.dream.erp.domain.entity.DespachoLote;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface DespachoLoteRepository extends JpaRepository<DespachoLote,String> {
    @EntityGraph(attributePaths={"lote","lote.orden","lote.orden.modelo","despacho"})
    List<DespachoLote> findByDespachoCodigo(String codigo);
    @Override @EntityGraph(attributePaths={"lote","lote.orden","lote.orden.modelo","despacho"})
    List<DespachoLote> findAll();
    @Query("select coalesce(sum(a.cantidad),0) from DespachoLote a where a.lote.id=:id and a.despacho.estado in :estados")
    long cantidadEnEstados(@Param("id") String id,@Param("estados") Collection<String> estados);
}
