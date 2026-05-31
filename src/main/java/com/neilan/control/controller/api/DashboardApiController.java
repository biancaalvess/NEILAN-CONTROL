package com.neilan.control.controller.api;

import com.neilan.control.dto.DashboardDto;
import com.neilan.control.dto.DtoMapper;
import com.neilan.control.service.FinanceiroService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardApiController {

    private final FinanceiroService financeiroService;

    public DashboardApiController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping
    public DashboardDto dashboard() {
        return new DashboardDto(
                financeiroService.calcularResumos(),
                financeiroService.listarServicos().stream()
                        .limit(5)
                        .map(DtoMapper::toDto)
                        .toList()
        );
    }
}
