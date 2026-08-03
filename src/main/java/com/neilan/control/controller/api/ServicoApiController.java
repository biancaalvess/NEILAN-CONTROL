package com.neilan.control.controller.api;

import com.neilan.control.dto.DtoMapper;
import com.neilan.control.dto.ServicoRealizadoDto;
import com.neilan.control.dto.ServicoRequest;
import com.neilan.control.model.ServicoRealizado;
import com.neilan.control.service.FinanceiroService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/servicos")
public class ServicoApiController {

    private final FinanceiroService financeiroService;

    public ServicoApiController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping
    public Map<String, Object> listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        LocalDate hoje = LocalDate.now();
        if (inicio == null) {
            inicio = hoje.withDayOfMonth(1);
        }
        if (fim == null) {
            fim = hoje;
        }

        List<ServicoRealizadoDto> servicos = financeiroService.listarServicosPorPeriodo(inicio, fim)
                .stream()
                .map(DtoMapper::toDto)
                .toList();

        BigDecimal total = servicos.stream()
                .map(ServicoRealizadoDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("servicos", servicos);
        response.put("inicio", inicio);
        response.put("fim", fim);
        response.put("totalPeriodo", total);
        response.put("quantidade", servicos.size());
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicoRealizadoDto registrar(@Valid @RequestBody ServicoRequest request) {
        try {
            ServicoRealizado servico = financeiroService.registrar(
                    request.tipoServicoId(),
                    request.clienteNome(),
                    request.placa(),
                    request.valor(),
                    request.custoInsumos(),
                    request.dataHora(),
                    request.observacoes()
            );
            return DtoMapper.toDto(servico);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
