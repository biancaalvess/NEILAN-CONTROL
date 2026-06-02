package com.neilan.control.repository;

import com.neilan.control.model.Custo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface CustoRepository extends JpaRepository<Custo, Long> {

    List<Custo> findByDataHoraBetweenOrderByDataHoraDesc(LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(c.valor), 0) FROM Custo c WHERE c.dataHora BETWEEN :inicio AND :fim")
    BigDecimal sumValorBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(c) FROM Custo c WHERE c.dataHora BETWEEN :inicio AND :fim")
    long countBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT c.descricao, SUM(c.valor), COUNT(c)
            FROM Custo c
            WHERE c.dataHora BETWEEN :inicio AND :fim
            GROUP BY c.descricao
            ORDER BY SUM(c.valor) DESC
            """)
    List<Object[]> sumByDescricaoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
