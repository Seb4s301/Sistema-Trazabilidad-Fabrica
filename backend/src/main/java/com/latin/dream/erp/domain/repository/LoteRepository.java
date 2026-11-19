package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio de dominio para Lote.
 */
@Repository
public interface LoteRepository extends JpaRepository<Lote, String> {


    @Override
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Lote> findById(String id);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    Optional<Lote> findByCodigo(String codigo);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"orden", "orden.modelo", "orden.registradoPor"})
    List<Lote> findAll();

    List<Lote> findByOrdenNumero(String numeroOrden);

}
