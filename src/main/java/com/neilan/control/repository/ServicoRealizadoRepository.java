package com.neilan.control.repository;

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

    @Query("SELECT COUNT(s) FROM ServicoRealizado s WHERE s.dataHora BETWEEN :inicio AND :fim")
    long countBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);

    @Query("""
            SELECT s.tipoServico.nome, SUM(s.valor), COUNT(s)
            FROM ServicoRealizado s
            WHERE s.dataHora BETWEEN :inicio AND :fim
            GROUP BY s.tipoServico.nome
            ORDER BY SUM(s.valor) DESC
            """)
    List<Object[]> sumByTipoServicoBetween(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
}
