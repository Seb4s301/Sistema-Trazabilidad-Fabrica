package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.LoteAvance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface LoteAvanceRepository extends JpaRepository<LoteAvance, String> {
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"lote", "responsable"})
    List<LoteAvance> findAllByOrderByInicioDesc();
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths={"responsable"})
    List<LoteAvance> findByLoteIdOrderByInicioDesc(String loteId);
}
