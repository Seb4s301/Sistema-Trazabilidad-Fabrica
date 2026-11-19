package com.latin.dream.erp.domain.repository;
import com.latin.dream.erp.domain.entity.LiquidacionComision;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;
public interface LiquidacionComisionRepository extends JpaRepository<LiquidacionComision,String> {
    @EntityGraph(attributePaths={"orden","liquidadoPor","pagadoPor"}) List<LiquidacionComision> findByPeriodo(String periodo);
    Optional<LiquidacionComision> findByOrdenId(String ordenId);
    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select l from LiquidacionComision l where l.id=:id")
    Optional<LiquidacionComision> bloquearPorId(@Param("id") String id);
}
