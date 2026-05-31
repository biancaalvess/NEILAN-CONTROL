package com.neilan.control.service;

import com.neilan.control.dto.ResumoLucro;
import com.neilan.control.model.ServicoRealizado;
import com.neilan.control.model.TipoServico;
import com.neilan.control.repository.ServicoRealizadoRepository;
import com.neilan.control.repository.TipoServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class FinanceiroService {

    private static final DateTimeFormatter MES_FORMAT = DateTimeFormatter.ofPattern("MM/yyyy");

    private final ServicoRealizadoRepository servicoRepository;
    private final TipoServicoRepository tipoServicoRepository;

    public FinanceiroService(ServicoRealizadoRepository servicoRepository,
                             TipoServicoRepository tipoServicoRepository) {
        this.servicoRepository = servicoRepository;
        this.tipoServicoRepository = tipoServicoRepository;
    }

    public List<ResumoLucro> calcularResumos() {
        LocalDate hoje = LocalDate.now();
        List<ResumoLucro> resumos = new ArrayList<>();

        resumos.add(resumoPeriodo("Diário", inicioDia(hoje), fimDia(hoje)));
        resumos.add(resumoPeriodo("Mensal", inicioMes(hoje), fimMes(hoje)));
        resumos.add(resumoPeriodo("Trimestral", inicioTrimestre(hoje), fimTrimestre(hoje)));
        resumos.add(resumoPeriodo("Anual", inicioAno(hoje), fimAno(hoje)));

        return resumos;
    }

    public ResumoLucro resumoPeriodo(String label, LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal total = servicoRepository.sumValorBetween(inicio, fim);
        long qtd = servicoRepository.countBetween(inicio, fim);
        return new ResumoLucro(label, total, qtd);
    }

    public List<Map<String, Object>> rankingServicos(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> rows = servicoRepository.sumByTipoServicoBetween(inicio, fim);
        List<Map<String, Object>> ranking = new ArrayList<>();
        BigDecimal totalGeral = servicoRepository.sumValorBetween(inicio, fim);

        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            BigDecimal total = (BigDecimal) row[1];
            item.put("nome", row[0]);
            item.put("total", total);
            item.put("quantidade", row[2]);
            if (totalGeral.compareTo(BigDecimal.ZERO) > 0) {
                item.put("percentual", total.multiply(BigDecimal.valueOf(100))
                        .divide(totalGeral, 1, java.math.RoundingMode.HALF_UP));
            } else {
                item.put("percentual", BigDecimal.ZERO);
            }
            ranking.add(item);
        }

        return ranking;
    }

    @Transactional
    public ServicoRealizado registrar(Long tipoServicoId, String clienteNome, String placa,
                                      BigDecimal valor, LocalDateTime dataHora, String observacoes) {
        TipoServico tipo = tipoServicoRepository.findById(tipoServicoId)
                .orElseThrow(() -> new IllegalArgumentException("Serviço não encontrado"));

        if (!tipo.isAtivo()) {
            throw new IllegalArgumentException("Este serviço está inativo");
        }

        ServicoRealizado servico = new ServicoRealizado();
        servico.setTipoServico(tipo);
        servico.setClienteNome(clienteNome != null ? clienteNome.trim() : null);
        servico.setPlaca(placa != null ? placa.trim().toUpperCase() : null);
        servico.setValor(valor);
        servico.setDataHora(dataHora != null ? dataHora : LocalDateTime.now());
        servico.setObservacoes(observacoes != null ? observacoes.trim() : null);

        return servicoRepository.save(servico);
    }

    @Transactional
    public TipoServico salvarTipo(TipoServico tipo) {
        return tipoServicoRepository.save(tipo);
    }

    @Transactional
    public void excluirTipo(Long id) {
        tipoServicoRepository.deleteById(id);
    }

    public Optional<TipoServico> buscarTipo(Long id) {
        return tipoServicoRepository.findById(id);
    }

    public List<TipoServico> listarTiposAtivos() {
        return tipoServicoRepository.findByAtivoTrueOrderByCategoriaAscNomeAsc();
    }

    public List<TipoServico> listarTodosTipos() {
        return tipoServicoRepository.findAllByOrderByCategoriaAscNomeAsc();
    }

    public List<ServicoRealizado> listarServicos() {
        return servicoRepository.findAllByOrderByDataHoraDesc();
    }

    public List<ServicoRealizado> listarServicosPorPeriodo(LocalDate inicio, LocalDate fim) {
        return servicoRepository.findByDataHoraBetweenOrderByDataHoraDesc(
                inicio.atStartOfDay(), fim.atTime(LocalTime.MAX));
    }

    public String gerarCsv(LocalDate inicio, LocalDate fim) {
        List<ServicoRealizado> servicos = listarServicosPorPeriodo(inicio, fim);
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Data;Hora;Servico;Categoria;Cliente;Placa;Valor;Observacoes\n");

        DateTimeFormatter dataFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter horaFmt = DateTimeFormatter.ofPattern("HH:mm");

        for (ServicoRealizado s : servicos) {
            sb.append(s.getId()).append(';');
            sb.append(s.getDataHora().format(dataFmt)).append(';');
            sb.append(s.getDataHora().format(horaFmt)).append(';');
            sb.append(escaparCsv(s.getTipoServico().getNome())).append(';');
            sb.append(escaparCsv(s.getTipoServico().getCategoria())).append(';');
            sb.append(escaparCsv(s.getClienteNome())).append(';');
            sb.append(escaparCsv(s.getPlaca())).append(';');
            sb.append(s.getValor()).append(';');
            sb.append(escaparCsv(s.getObservacoes())).append('\n');
        }

        return sb.toString();
    }

    private String escaparCsv(String valor) {
        if (valor == null) {
            return "";
        }
        if (valor.contains(";") || valor.contains("\"") || valor.contains("\n")) {
            return "\"" + valor.replace("\"", "\"\"") + "\"";
        }
        return valor;
    }

    public LocalDateTime inicioDia(LocalDate data) {
        return data.atStartOfDay();
    }

    public LocalDateTime fimDia(LocalDate data) {
        return data.atTime(LocalTime.MAX);
    }

    public LocalDateTime inicioMes(LocalDate data) {
        return data.withDayOfMonth(1).atStartOfDay();
    }

    public LocalDateTime fimMes(LocalDate data) {
        YearMonth ym = YearMonth.from(data);
        return ym.atEndOfMonth().atTime(LocalTime.MAX);
    }

    public LocalDateTime inicioTrimestre(LocalDate data) {
        int mes = ((data.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(data.getYear(), mes, 1).atStartOfDay();
    }

    public LocalDateTime fimTrimestre(LocalDate data) {
        int mesInicio = ((data.getMonthValue() - 1) / 3) * 3 + 1;
        int mesFim = mesInicio + 2;
        YearMonth ym = YearMonth.of(data.getYear(), mesFim);
        return ym.atEndOfMonth().atTime(LocalTime.MAX);
    }

    public LocalDateTime inicioAno(LocalDate data) {
        return LocalDate.of(data.getYear(), 1, 1).atStartOfDay();
    }

    public LocalDateTime fimAno(LocalDate data) {
        return LocalDate.of(data.getYear(), 12, 31).atTime(LocalTime.MAX);
    }
}
