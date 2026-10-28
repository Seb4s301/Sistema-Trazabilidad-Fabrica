package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, String> {
    Optional<Despacho> findByCodigo(String codigo);
    List<Despacho> findByEstado(String estado);
    List<Despacho> findAllByOrderByCreadoEnDesc();
}
