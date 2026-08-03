package com.neilan.control.dto;

import java.math.BigDecimal;

public interface AgregacaoServicoProjection {

    Integer getAno();

    Integer getMes();

    Integer getDia();

    BigDecimal getReceita();

    BigDecimal getCustoInsumos();

    Long getQuantidade();
}
