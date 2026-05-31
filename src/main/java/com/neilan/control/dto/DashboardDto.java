package com.neilan.control.dto;

import java.util.List;
import java.util.Map;

public record DashboardDto(
        List<ResumoLucro> resumos,
        List<ServicoRealizadoDto> ultimosServicos
) {
}
