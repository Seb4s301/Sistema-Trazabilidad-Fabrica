package com.latin.dream.erp.domain.repository;

import com.latin.dream.erp.domain.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Interfaz de repositorio de dominio para Usuario.
 * Define el contrato de acceso a datos sin exponer la tecnología subyacente.
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    Optional<Usuario> findByCorreo(String correo);

    boolean existsByCorreo(String correo);
}
