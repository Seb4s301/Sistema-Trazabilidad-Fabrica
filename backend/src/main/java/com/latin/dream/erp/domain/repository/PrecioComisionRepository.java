package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.PrecioComision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PrecioComisionRepository extends JpaRepository<PrecioComision, String> {
    List<PrecioComision> findByModeloId(String modeloId);
}
