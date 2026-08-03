package com.neilan.control.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicos_realizados")
public class ServicoRealizado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tipo_servico_id", nullable = false)
    @NotNull
    private TipoServico tipoServico;

    @Column(length = 120)
    private String clienteNome;

    @Column(length = 10)
    private String placa;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    /** Custo de peças/insumos consumidos neste serviço (custo operacional direto). */
    @NotNull
    @DecimalMin("0.00")
    @Column(name = "custo_insumos", nullable = false, precision = 10, scale = 2)
    private BigDecimal custoInsumos = BigDecimal.ZERO;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime dataHora;

    @Column(length = 500)
    private String observacoes;

    public ServicoRealizado() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoServico getTipoServico() {
        return tipoServico;
    }

    public void setTipoServico(TipoServico tipoServico) {
        this.tipoServico = tipoServico;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public String getPlaca() {
        return placa;
    }

    public void setPlaca(String placa) {
        this.placa = placa;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public BigDecimal getCustoInsumos() {
        return custoInsumos;
    }

    public void setCustoInsumos(BigDecimal custoInsumos) {
        this.custoInsumos = custoInsumos != null ? custoInsumos : BigDecimal.ZERO;
    }

    public BigDecimal getValorTotal() {
        return valor;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
