package com.neilan.control.dto;

import java.math.BigDecimal;

public record DreResumoDto(
        BigDecimal receitaBruta,
        BigDecimal custoOperacionalDireto,
        BigDecimal custosFixosVariaveis,
        BigDecimal custosTotais,
        BigDecimal lucroLiquido,
        BigDecimal margemPercentual,
        long quantidadeServicos,
        long quantidadeCustos
) {
}
