package com.neilan.control.dto;

import com.neilan.control.model.Custo;
import com.neilan.control.model.ServicoRealizado;
import com.neilan.control.model.TipoServico;

public final class DtoMapper {

    private DtoMapper() {
    }

    public static TipoServicoDto toDto(TipoServico t) {
        return new TipoServicoDto(t.getId(), t.getNome(), t.getDescricao(),
                t.getCategoria(), t.getPreco(), t.isAtivo());
    }

    public static ServicoRealizadoDto toDto(ServicoRealizado s) {
        TipoServico tipo = s.getTipoServico();
        return new ServicoRealizadoDto(
                s.getId(),
                tipo.getId(),
                tipo.getNome(),
                tipo.getCategoria(),
                s.getClienteNome(),
                s.getPlaca(),
                s.getValor(),
                s.getDataHora(),
                s.getObservacoes()
        );
    }

    public static CustoDto toDto(Custo c) {
        return new CustoDto(
                c.getId(),
                c.getDescricao(),
                c.getValor(),
                c.getDataHora(),
                c.getObservacoes()
        );
    }
}
