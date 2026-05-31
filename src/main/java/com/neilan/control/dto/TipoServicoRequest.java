package com.neilan.control.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record TipoServicoRequest(
        @NotBlank String nome,
        String descricao,
        @NotBlank String categoria,
        @NotNull @DecimalMin("0.01") BigDecimal preco,
        boolean ativo
) {
}
