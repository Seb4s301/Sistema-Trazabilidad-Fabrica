package com.latin.dream.erp.domain.repository;
import com.latin.dream.erp.domain.entity.HistorialOrden;
import org.springframework.data.jpa.repository.*;
import java.util.List;
public interface HistorialOrdenRepository extends JpaRepository<HistorialOrden,String> {
    @EntityGraph(attributePaths={"usuario"})
    List<HistorialOrden> findByOrdenIdOrderByFechaDesc(String ordenId);
    void deleteByOrdenId(String ordenId);
}
