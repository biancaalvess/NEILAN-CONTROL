package com.neilan.control.controller.api;

import com.neilan.control.dto.DtoMapper;
import com.neilan.control.dto.RelatorioDto;
import com.neilan.control.service.FinanceiroService;
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

    private final FinanceiroService financeiroService;

    public RelatorioApiController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping
    public RelatorioDto relatorio(@RequestParam(defaultValue = "mensal") String periodo) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio;
        LocalDateTime fim;
        String titulo;

        switch (periodo) {
            case "diario" -> {
                inicio = financeiroService.inicioDia(hoje);
                fim = financeiroService.fimDia(hoje);
                titulo = "Relatório Diário - " + hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "trimestral" -> {
                inicio = financeiroService.inicioTrimestre(hoje);
                fim = financeiroService.fimTrimestre(hoje);
                titulo = "Relatório Trimestral";
            }
            case "anual" -> {
                inicio = financeiroService.inicioAno(hoje);
                fim = financeiroService.fimAno(hoje);
                titulo = "Relatório Anual - " + hoje.getYear();
            }
            default -> {
                inicio = financeiroService.inicioMes(hoje);
                fim = financeiroService.fimMes(hoje);
                titulo = "Relatório Mensal - " + hoje.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                periodo = "mensal";
            }
        }

        return new RelatorioDto(
                periodo,
                titulo,
                financeiroService.resumoPeriodo(periodo, inicio, fim),
                financeiroService.rankingServicos(inicio, fim),
                financeiroService.listarServicosPorPeriodo(inicio.toLocalDate(), fim.toLocalDate())
                        .stream()
                        .map(DtoMapper::toDto)
                        .toList()
        );
    }
}
