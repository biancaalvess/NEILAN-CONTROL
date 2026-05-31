package com.neilan.control.controller.api;

import com.neilan.control.dto.DtoMapper;
import com.neilan.control.dto.TipoServicoDto;
import com.neilan.control.dto.TipoServicoRequest;
import com.neilan.control.model.TipoServico;
import com.neilan.control.service.FinanceiroService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-servico")
public class TipoServicoApiController {

    private final FinanceiroService financeiroService;

    public TipoServicoApiController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping
    public List<TipoServicoDto> listar(@RequestParam(defaultValue = "false") boolean apenasAtivos) {
        List<TipoServico> tipos = apenasAtivos
                ? financeiroService.listarTiposAtivos()
                : financeiroService.listarTodosTipos();
        return tipos.stream().map(DtoMapper::toDto).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TipoServicoDto criar(@Valid @RequestBody TipoServicoRequest request) {
        TipoServico tipo = new TipoServico();
        applyRequest(tipo, request);
        return DtoMapper.toDto(financeiroService.salvarTipo(tipo));
    }

    @PutMapping("/{id}")
    public TipoServicoDto atualizar(@PathVariable Long id, @Valid @RequestBody TipoServicoRequest request) {
        TipoServico tipo = financeiroService.buscarTipo(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado"));
        applyRequest(tipo, request);
        return DtoMapper.toDto(financeiroService.salvarTipo(tipo));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void excluir(@PathVariable Long id) {
        if (financeiroService.buscarTipo(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Serviço não encontrado");
        }
        financeiroService.excluirTipo(id);
    }

    private void applyRequest(TipoServico tipo, TipoServicoRequest request) {
        tipo.setNome(request.nome());
        tipo.setDescricao(request.descricao());
        tipo.setCategoria(request.categoria());
        tipo.setPreco(request.preco());
        tipo.setAtivo(request.ativo());
    }
}
