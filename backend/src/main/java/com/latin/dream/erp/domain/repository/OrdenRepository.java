package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio de dominio para Orden.
 */
@Repository
public interface OrdenRepository extends JpaRepository<Orden, String>, JpaSpecificationExecutor<Orden> {

    Optional<Orden> findByNumero(String numero);

    List<Orden> findByEstado(String estado);

    List<Orden> findByPrioridad(String prioridad);

    boolean existsByNumero(String numero);

    /**
     * Devuelve la secuencia numérica máxima de las órdenes de un año dado.
     * Ej.: numero = "OP-2026-0420" → 420.
     */
    @Query("SELECT MAX(CAST(SUBSTRING(o.numero, 9, 4) AS Integer)) " +
           "FROM Orden o WHERE o.numero LIKE CONCAT('OP-', :anio, '-%')")
    Integer findMaxSecuenciaPorAnio(@Param("anio") String anio);
}
