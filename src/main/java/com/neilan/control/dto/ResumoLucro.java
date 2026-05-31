package com.neilan.control.dto;

import java.math.BigDecimal;

public class ResumoLucro {

    private final String periodo;
    private final BigDecimal total;
    private final long quantidade;

    public ResumoLucro(String periodo, BigDecimal total, long quantidade) {
        this.periodo = periodo;
        this.total = total != null ? total : BigDecimal.ZERO;
        this.quantidade = quantidade;
    }

    public String getPeriodo() {
        return periodo;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public long getQuantidade() {
        return quantidade;
    }
}
