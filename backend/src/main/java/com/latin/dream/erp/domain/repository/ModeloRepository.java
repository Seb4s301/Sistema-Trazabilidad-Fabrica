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

    boolean existsByCodigo(String codigo);
}
