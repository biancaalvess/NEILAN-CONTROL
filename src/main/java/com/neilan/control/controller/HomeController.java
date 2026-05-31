package com.neilan.control.controller;

import com.neilan.control.model.ServicoRealizado;
import com.neilan.control.model.TipoServico;
import com.neilan.control.service.FinanceiroService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
public class HomeController {

    private final FinanceiroService financeiroService;

    public HomeController(FinanceiroService financeiroService) {
        this.financeiroService = financeiroService;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("resumos", financeiroService.calcularResumos());
        model.addAttribute("ultimosServicos", financeiroService.listarServicos().stream().limit(5).toList());
        return "index";
    }

    @GetMapping("/registrar")
    public String registrarForm(Model model) {
        model.addAttribute("tiposServico", financeiroService.listarTiposAtivos());
        model.addAttribute("servicoForm", new ServicoForm());
        return "registrar";
    }

    @PostMapping("/registrar")
    public String registrarSubmit(@ModelAttribute("servicoForm") ServicoForm form,
                                  BindingResult bindingResult,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {
        if (form.getTipoServicoId() == null) {
            bindingResult.rejectValue("tipoServicoId", "required", "Selecione um serviço");
        }
        if (form.getValor() == null || form.getValor().compareTo(BigDecimal.ZERO) <= 0) {
            bindingResult.rejectValue("valor", "required", "Informe um valor válido");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposServico", financeiroService.listarTiposAtivos());
            return "registrar";
        }

        try {
            financeiroService.registrar(
                    form.getTipoServicoId(),
                    form.getClienteNome(),
                    form.getPlaca(),
                    form.getValor(),
                    form.getDataHora(),
                    form.getObservacoes()
            );
            redirectAttributes.addFlashAttribute("sucesso", "Serviço registrado com sucesso!");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("erro", e.getMessage());
        }

        return "redirect:/registrar";
    }

    @GetMapping("/servicos")
    public String listarServicos(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
                                 Model model) {
        LocalDate hoje = LocalDate.now();
        if (inicio == null) {
            inicio = hoje.withDayOfMonth(1);
        }
        if (fim == null) {
            fim = hoje;
        }

        List<ServicoRealizado> servicos = financeiroService.listarServicosPorPeriodo(inicio, fim);
        BigDecimal total = servicos.stream()
                .map(ServicoRealizado::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("servicos", servicos);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("totalPeriodo", total);
        model.addAttribute("quantidade", servicos.size());
        return "servicos";
    }

    @GetMapping("/relatorio")
    public String relatorio(@RequestParam(defaultValue = "mensal") String periodo, Model model) {
        LocalDate hoje = LocalDate.now();
        LocalDateTime inicio;
        LocalDateTime fim;
        String titulo;

        switch (periodo) {
            case "diario" -> {
                inicio = financeiroService.inicioDia(hoje);
                fim = financeiroService.fimDia(hoje);
                titulo = "Relatório Diário - " + hoje.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            }
            case "trimestral" -> {
                inicio = financeiroService.inicioTrimestre(hoje);
                fim = financeiroService.fimTrimestre(hoje);
                titulo = "Relatório Trimestral";
            }
            case "anual" -> {
                inicio = financeiroService.inicioAno(hoje);
                fim = financeiroService.fimAno(hoje);
                titulo = "Relatório Anual - " + hoje.getYear();
            }
            default -> {
                inicio = financeiroService.inicioMes(hoje);
                fim = financeiroService.fimMes(hoje);
                titulo = "Relatório Mensal - " + hoje.format(DateTimeFormatter.ofPattern("MM/yyyy"));
                periodo = "mensal";
            }
        }

        model.addAttribute("periodo", periodo);
        model.addAttribute("titulo", titulo);
        model.addAttribute("resumo", financeiroService.resumoPeriodo(periodo, inicio, fim));
        model.addAttribute("ranking", financeiroService.rankingServicos(inicio, fim));
        model.addAttribute("servicos", financeiroService.listarServicosPorPeriodo(
                inicio.toLocalDate(), fim.toLocalDate()));
        return "relatorio";
    }

    @GetMapping("/configuracao")
    public String configuracao(Model model) {
        model.addAttribute("tiposServico", financeiroService.listarTodosTipos());
        model.addAttribute("novoServico", new TipoServico());
        return "configuracao";
    }

    @PostMapping("/configuracao/novo")
    public String novoServico(@Valid @ModelAttribute("novoServico") TipoServico tipo,
                              BindingResult bindingResult,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("tiposServico", financeiroService.listarTodosTipos());
            return "configuracao";
        }
        financeiroService.salvarTipo(tipo);
        redirectAttributes.addFlashAttribute("sucesso", "Serviço adicionado!");
        return "redirect:/configuracao";
    }

    @PostMapping("/configuracao/{id}/editar")
    public String editarServico(@PathVariable Long id,
                                @RequestParam String nome,
                                @RequestParam String categoria,
                                @RequestParam String descricao,
                                @RequestParam BigDecimal preco,
                                @RequestParam(defaultValue = "false") boolean ativo,
                                RedirectAttributes redirectAttributes) {
        TipoServico tipo = financeiroService.buscarTipo(id)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        tipo.setNome(nome);
        tipo.setCategoria(categoria);
        tipo.setDescricao(descricao);
        tipo.setPreco(preco);
        tipo.setAtivo(ativo);
        financeiroService.salvarTipo(tipo);

        redirectAttributes.addFlashAttribute("sucesso", "Serviço atualizado!");
        return "redirect:/configuracao";
    }

    @PostMapping("/configuracao/{id}/excluir")
    public String excluirServico(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        financeiroService.excluirTipo(id);
        redirectAttributes.addFlashAttribute("sucesso", "Serviço removido!");
        return "redirect:/configuracao";
    }

    @GetMapping("/export/csv")
    public ResponseEntity<byte[]> exportarCsv(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {

        LocalDate hoje = LocalDate.now();
        if (inicio == null) {
            inicio = hoje.withDayOfMonth(1);
        }
        if (fim == null) {
            fim = hoje;
        }

        String csv = financeiroService.gerarCsv(inicio, fim);
        String filename = "neilan-servicos-" + inicio + "-" + fim + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .body(("\uFEFF" + csv).getBytes(StandardCharsets.UTF_8));
    }

    public static class ServicoForm {
        private Long tipoServicoId;
        private String clienteNome;
        private String placa;
        private BigDecimal valor;
        private LocalDateTime dataHora = LocalDateTime.now();
        private String observacoes;

        public Long getTipoServicoId() {
            return tipoServicoId;
        }

        public void setTipoServicoId(Long tipoServicoId) {
            this.tipoServicoId = tipoServicoId;
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
}
