package com.neilan.control.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ServicoRealizadoDto(
        Long id,
        Long tipoServicoId,
        String tipoServicoNome,
        String categoria,
        String clienteNome,
        String placa,
        BigDecimal valor,
        LocalDateTime dataHora,
        String observacoes
) {
}
