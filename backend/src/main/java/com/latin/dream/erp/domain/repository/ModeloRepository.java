package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.Modelo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Interfaz de repositorio de dominio para Modelo (catálogo).
 */
@Repository
public interface ModeloRepository extends JpaRepository<Modelo, String> {

    List<Modelo> findByActivoTrue();
    boolean existsByCodigoAndIdNot(String codigo,String id);
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select m from Modelo m where m.id=:id")
    java.util.Optional<Modelo> bloquearPorId(@org.springframework.data.repository.query.Param("id") String id);

}
