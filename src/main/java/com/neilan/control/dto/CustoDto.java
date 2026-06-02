package com.neilan.control.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CustoDto(
        Long id,
        String descricao,
        BigDecimal valor,
        LocalDateTime dataHora,
        String observacoes
) {
}
