package com.neilan.control.dto;

import java.util.List;
import java.util.Map;

public record RelatorioCustosDto(
        String periodo,
        String titulo,
        ResumoLucro resumo,
        List<Map<String, Object>> ranking,
        List<CustoDto> custos
) {
}
