package com.neilan.control.dto;

import java.math.BigDecimal;

public record LinhaDreDto(
        String periodoLabel,
        String periodoChave,
        BigDecimal receitaBruta,
        BigDecimal custoOperacionalDireto,
        BigDecimal custosFixosVariaveis,
        BigDecimal custosTotais,
        BigDecimal lucroLiquido,
        BigDecimal margemPercentual,
        long quantidadeServicos
) {
}
