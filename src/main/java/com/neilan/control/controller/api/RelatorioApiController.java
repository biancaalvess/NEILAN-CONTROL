package com.neilan.control.controller.api;

import com.neilan.control.dto.DtoMapper;
import com.neilan.control.dto.RelatorioCustosDto;
import com.neilan.control.dto.RelatorioDto;
import com.neilan.control.dto.RelatorioFinanceiroDto;
import com.neilan.control.service.FinanceiroService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/relatorio")
public class RelatorioApiController {

    private record PeriodoInfo(String periodo, String titulo, LocalDateTime inicio, LocalDateTime fim) {
    }

    private final FinanceiroService financeiroService;

    public RelatorioApiController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/financeiro")
    public RelatorioFinanceiroDto relatorioFinanceiro(@RequestParam(defaultValue = "mensal") String periodo) {
        PeriodoInfo info = resolverPeriodo(periodo);
        return financeiroService.montarRelatorioFinanceiro(
                info.periodo(), info.titulo(), info.inicio(), info.fim());
    }

    @GetMapping("/servicos")
    public RelatorioDto relatorioServicos(@RequestParam(defaultValue = "mensal") String periodo) {
        PeriodoInfo info = resolverPeriodo(periodo);
        return new RelatorioDto(
                info.periodo(),
                info.titulo(),
                financeiroService.resumoLucroPeriodo(info.periodo(), info.inicio(), info.fim()),
                financeiroService.rankingServicos(info.inicio(), info.fim()),
                financeiroService.listarServicosPorPeriodo(info.inicio().toLocalDate(), info.fim().toLocalDate())
                        .stream()
                        .map(DtoMapper::toDto)
                        .toList()
        );
    }

    @GetMapping("/custos")
    public RelatorioCustosDto relatorioCustos(@RequestParam(defaultValue = "mensal") String periodo) {
        PeriodoInfo info = resolverPeriodo(periodo, "Custos");
        return new RelatorioCustosDto(
                info.periodo(),
                info.titulo(),
                financeiroService.resumoCustoPeriodo(info.periodo(), info.inicio(), info.fim()),
                financeiroService.rankingCustos(info.inicio(), info.fim()),
                financeiroService.listarCustosPorPeriodo(info.inicio().toLocalDate(), info.fim().toLocalDate())
                        .stream()
                        .map(DtoMapper::toDto)
                        .toList()
        );
    }

    /** @deprecated use /api/relatorio/financeiro */
    @GetMapping
    public RelatorioFinanceiroDto relatorio(@RequestParam(defaultValue = "mensal") String periodo) {
        return relatorioFinanceiro(periodo);
    }

    private PeriodoInfo resolverPeriodo(String periodo) {
        return resolverPeriodo(periodo, null);
    }

    private PeriodoInfo resolverPeriodo(String periodo, String prefixo) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio;
        LocalDateTime fim;
        String titulo;
        String sufixo = prefixo != null ? " de " + prefixo : "";

        switch (periodo) {
            case "diario" -> {
                inicio = financeiroService.inicioDia(hoje);
                fim = financeiroService.fimDia(hoje);
                titulo = "Relatório Diário" + sufixo + " - " + hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "trimestral" -> {
                inicio = financeiroService.inicioTrimestre(hoje);
                fim = financeiroService.fimTrimestre(hoje);
                titulo = "Relatório Trimestral" + sufixo;
            }
            case "anual" -> {
                inicio = financeiroService.inicioAno(hoje);
                fim = financeiroService.fimAno(hoje);
                titulo = "Relatório Anual" + sufixo + " - " + hoje.getYear();
            }
            default -> {
                inicio = financeiroService.inicioMes(hoje);
                fim = financeiroService.fimMes(hoje);
                titulo = "Relatório Mensal" + sufixo + " - " + hoje.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                periodo = "mensal";
            }
        }

        return new PeriodoInfo(periodo, titulo, inicio, fim);
    }
}
