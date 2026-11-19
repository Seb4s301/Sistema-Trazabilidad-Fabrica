package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.Orden;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Interfaz de repositorio de dominio para Orden.
 */
@Repository
public interface OrdenRepository extends JpaRepository<Orden, String> {

    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"modelo", "registradoPor"})
    Optional<Orden> findByNumero(String numero);

    @Override
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"modelo", "registradoPor"})
    List<Orden> findAll();

    Optional<Orden> findBySolicitudId(String solicitudId);
    boolean existsByModeloId(String modeloId);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from Orden o where o.id = :id")
    Optional<Orden> bloquearPorId(@Param("id") String id);

    boolean existsByNumero(String numero);

    /**
     * Devuelve la secuencia numérica máxima de las órdenes de un año dado.
     * Ej.: numero = "OP-2026-0420" → 420.
     */
    @Query("SELECT MAX(CAST(SUBSTRING(o.numero, 9) AS Integer)) " +
           "FROM Orden o WHERE o.numero LIKE CONCAT('OP-', :anio, '-%')")
    Integer findMaxSecuenciaPorAnio(@Param("anio") String anio);
}
