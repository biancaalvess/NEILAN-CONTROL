package com.neilan.control.controller.api;

import com.neilan.control.dto.CustoDto;
import com.neilan.control.dto.CustoRequest;
import com.neilan.control.dto.DtoMapper;
import com.neilan.control.model.Custo;
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
@RequestMapping("/api/custos")
public class CustoApiController {

    private final FinanceiroService financeiroService;

    public CustoApiController(FinanceiroService financeiroService) {
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

        List<CustoDto> custos = financeiroService.listarCustosPorPeriodo(inicio, fim)
                .stream()
                .map(DtoMapper::toDto)
                .toList();

        BigDecimal total = custos.stream()
                .map(CustoDto::valor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> response = new HashMap<>();
        response.put("custos", custos);
        response.put("inicio", inicio);
        response.put("fim", fim);
        response.put("totalPeriodo", total);
        response.put("quantidade", custos.size());
        return response;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CustoDto registrar(@Valid @RequestBody CustoRequest request) {
        try {
            Custo custo = financeiroService.registrarCusto(
                    request.descricao(),
                    request.valor(),
                    request.dataHora(),
                    request.observacoes()
            );
            return DtoMapper.toDto(custo);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        try {
            financeiroService.excluirCusto(id);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
        }
    }
}
