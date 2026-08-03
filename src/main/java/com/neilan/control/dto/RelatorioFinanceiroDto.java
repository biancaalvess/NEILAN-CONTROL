package com.neilan.control.dto;

import java.util.List;
import java.util.Map;

public record RelatorioFinanceiroDto(
        String periodo,
        String titulo,
        DreResumoDto dre,
        List<LinhaDreDto> linhas,
        List<Map<String, Object>> rankingServicos,
        List<Map<String, Object>> rankingCustos
) {
}
