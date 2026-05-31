package com.neilan.control.dto;

import java.math.BigDecimal;

public record TipoServicoDto(
        Long id,
        String nome,
        String descricao,
        String categoria,
        BigDecimal preco,
        boolean ativo
) {
}
