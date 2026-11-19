package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.Despacho;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface DespachoRepository extends JpaRepository<Despacho, String> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Despacho> findByCodigo(String codigo);
    List<Despacho> findAllByOrderByCreadoEnDesc();
    Optional<Despacho> findBySolicitudId(String solicitudId);
}
