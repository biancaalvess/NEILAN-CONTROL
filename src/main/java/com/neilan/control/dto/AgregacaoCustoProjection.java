package com.neilan.control.dto;

import java.math.BigDecimal;

public interface AgregacaoCustoProjection {

    Integer getAno();

    Integer getMes();

    Integer getDia();

    BigDecimal getTotal();

    Long getQuantidade();
}
