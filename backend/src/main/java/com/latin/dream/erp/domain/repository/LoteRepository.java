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

    List<Lote> findByAreaActual(String areaActual);

    Optional<Lote> findByCodigo(String codigo);

    List<Lote> findByOrdenNumero(String numeroOrden);

    boolean existsByCodigo(String codigo);
}
