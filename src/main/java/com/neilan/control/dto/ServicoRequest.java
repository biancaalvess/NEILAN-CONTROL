package com.neilan.control.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoRequest(
        @NotNull Long tipoServicoId,
        String clienteNome,
        String placa,
        @NotNull @DecimalMin("0.01") BigDecimal valor,
        @DecimalMin("0.00") BigDecimal custoInsumos,
        LocalDateTime dataHora,
        String observacoes
) {
}
