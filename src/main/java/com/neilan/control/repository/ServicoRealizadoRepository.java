package com.neilan.control.repository;

import com.neilan.control.dto.AgregacaoServicoProjection;
import com.neilan.control.model.ServicoRealizado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface ServicoRealizadoRepository extends JpaRepository<ServicoRealizado, Long> {

    List<ServicoRealizado> findAllByOrderByDataHoraDesc();

    List<ServicoRealizado> findByDataHoraBetweenOrderByDataHoraDesc(
            LocalDateTime inicio, LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(s.valor), 0) FROM ServicoRealizado s WHERE s.dataHora BETWEEN :inicio AND :fim")
    BigDecimal sumValorBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COALESCE(SUM(s.custoInsumos), 0) FROM ServicoRealizado s WHERE s.dataHora BETWEEN :inicio AND :fim")
    BigDecimal sumCustoInsumosBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("SELECT COUNT(s) FROM ServicoRealizado s WHERE s.dataHora BETWEEN :inicio AND :fim")
    long countBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT s.tipoServico.nome AS nome, SUM(s.valor) AS total, COUNT(s) AS quantidade
            FROM ServicoRealizado s
            WHERE s.dataHora BETWEEN :inicio AND :fim
            GROUP BY s.tipoServico.nome
            ORDER BY SUM(s.valor) DESC
            """)
    List<Object[]> sumByTipoServicoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT
                YEAR(s.dataHora) AS ano,
                MONTH(s.dataHora) AS mes,
                DAY(s.dataHora) AS dia,
                COALESCE(SUM(s.valor), 0) AS receita,
                COALESCE(SUM(s.custoInsumos), 0) AS custoInsumos,
                COUNT(s) AS quantidade
            FROM ServicoRealizado s
            WHERE s.dataHora BETWEEN :inicio AND :fim
            GROUP BY YEAR(s.dataHora), MONTH(s.dataHora), DAY(s.dataHora)
            ORDER BY YEAR(s.dataHora), MONTH(s.dataHora), DAY(s.dataHora)
            """)
    List<AgregacaoServicoProjection> agregarPorDia(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT
                YEAR(s.dataHora) AS ano,
                MONTH(s.dataHora) AS mes,
                1 AS dia,
                COALESCE(SUM(s.valor), 0) AS receita,
                COALESCE(SUM(s.custoInsumos), 0) AS custoInsumos,
                COUNT(s) AS quantidade
            FROM ServicoRealizado s
            WHERE s.dataHora BETWEEN :inicio AND :fim
            GROUP BY YEAR(s.dataHora), MONTH(s.dataHora)
            ORDER BY YEAR(s.dataHora), MONTH(s.dataHora)
            """)
    List<AgregacaoServicoProjection> agregarPorMes(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
