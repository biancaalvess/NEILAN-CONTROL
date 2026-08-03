package com.neilan.control.service;

import com.neilan.control.dto.AgregacaoCustoProjection;
import com.neilan.control.dto.AgregacaoServicoProjection;
import com.neilan.control.dto.DreResumoDto;
import com.neilan.control.dto.LinhaDreDto;
import com.neilan.control.dto.RelatorioFinanceiroDto;
import com.neilan.control.dto.ResumoLucro;
import com.neilan.control.model.Custo;
import com.neilan.control.model.ServicoRealizado;
import com.neilan.control.model.TipoServico;
import com.neilan.control.repository.CustoRepository;
import com.neilan.control.repository.ServicoRealizadoRepository;
import com.neilan.control.repository.TipoServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
    private static final DateTimeFormatter DIA_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final ServicoRealizadoRepository servicoRepository;
    private final TipoServicoRepository tipoServicoRepository;
    private final CustoRepository custoRepository;

    public FinanceiroService(ServicoRealizadoRepository servicoRepository,
                             TipoServicoRepository tipoServicoRepository,
                             CustoRepository custoRepository) {
        this.servicoRepository = servicoRepository;
        this.tipoServicoRepository = tipoServicoRepository;
        this.custoRepository = custoRepository;
    }

    public RelatorioFinanceiroDto montarRelatorioFinanceiro(String periodo, String titulo,
                                                            LocalDateTime inicio, LocalDateTime fim) {
        DreResumoDto dre = calcularDre(inicio, fim);
        List<LinhaDreDto> linhas = montarLinhasDre(periodo, inicio, fim);

        return new RelatorioFinanceiroDto(
                periodo,
                titulo,
                dre,
                linhas,
                rankingServicos(inicio, fim),
                rankingCustos(inicio, fim)
        );
    }

    public DreResumoDto calcularDre(LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal receitaBruta = servicoRepository.sumValorBetween(inicio, fim);
        BigDecimal custoOperacionalDireto = servicoRepository.sumCustoInsumosBetween(inicio, fim);
        BigDecimal custosFixosVariaveis = custoRepository.sumValorBetween(inicio, fim);
        BigDecimal custosTotais = custoOperacionalDireto.add(custosFixosVariaveis);
        BigDecimal lucroLiquido = receitaBruta.subtract(custosTotais);
        BigDecimal margemPercentual = calcularMargem(lucroLiquido, receitaBruta);

        return new DreResumoDto(
                receitaBruta,
                custoOperacionalDireto,
                custosFixosVariaveis,
                custosTotais,
                lucroLiquido,
                margemPercentual,
                servicoRepository.countBetween(inicio, fim),
                custoRepository.countBetween(inicio, fim)
        );
    }

    public List<LinhaDreDto> montarLinhasDre(String periodo, LocalDateTime inicio, LocalDateTime fim) {
        boolean agruparPorMes = "trimestral".equals(periodo) || "anual".equals(periodo);

        List<AgregacaoServicoProjection> servicos = agruparPorMes
                ? servicoRepository.agregarPorMes(inicio, fim)
                : servicoRepository.agregarPorDia(inicio, fim);

        List<AgregacaoCustoProjection> custos = agruparPorMes
                ? custoRepository.agregarPorMes(inicio, fim)
                : custoRepository.agregarPorDia(inicio, fim);

        Map<String, LinhaAcumulada> acumulado = new LinkedHashMap<>();

        for (AgregacaoServicoProjection row : servicos) {
            String chave = chavePeriodo(row.getAno(), row.getMes(), row.getDia(), agruparPorMes);
            LinhaAcumulada linha = acumulado.computeIfAbsent(chave, k -> new LinhaAcumulada());
            linha.receita = linha.receita.add(nullSafe(row.getReceita()));
            linha.custoInsumos = linha.custoInsumos.add(nullSafe(row.getCustoInsumos()));
            linha.quantidadeServicos += row.getQuantidade() != null ? row.getQuantidade() : 0;
            linha.ano = row.getAno();
            linha.mes = row.getMes();
            linha.dia = row.getDia();
        }

        for (AgregacaoCustoProjection row : custos) {
            String chave = chavePeriodo(row.getAno(), row.getMes(), row.getDia(), agruparPorMes);
            LinhaAcumulada linha = acumulado.computeIfAbsent(chave, k -> new LinhaAcumulada());
            linha.custosFixos = linha.custosFixos.add(nullSafe(row.getTotal()));
            linha.ano = row.getAno();
            linha.mes = row.getMes();
            linha.dia = row.getDia();
        }

        List<LinhaDreDto> linhas = new ArrayList<>();
        for (Map.Entry<String, LinhaAcumulada> entry : acumulado.entrySet()) {
            LinhaAcumulada linha = entry.getValue();
            BigDecimal custosTotais = linha.custoInsumos.add(linha.custosFixos);
            BigDecimal lucro = linha.receita.subtract(custosTotais);

            linhas.add(new LinhaDreDto(
                    formatarLabelPeriodo(linha.ano, linha.mes, linha.dia, agruparPorMes),
                    entry.getKey(),
                    linha.receita,
                    linha.custoInsumos,
                    linha.custosFixos,
                    custosTotais,
                    lucro,
                    calcularMargem(lucro, linha.receita),
                    linha.quantidadeServicos
            ));
        }

        return linhas;
    }

    public List<ResumoLucro> calcularResumos() {
        LocalDate hoje = LocalDate.now();
        List<ResumoLucro> resumos = new ArrayList<>();

        resumos.add(resumoLucroPeriodo("Diário", inicioDia(hoje), fimDia(hoje)));
        resumos.add(resumoLucroPeriodo("Mensal", inicioMes(hoje), fimMes(hoje)));
        resumos.add(resumoLucroPeriodo("Trimestral", inicioTrimestre(hoje), fimTrimestre(hoje)));
        resumos.add(resumoLucroPeriodo("Anual", inicioAno(hoje), fimAno(hoje)));

        return resumos;
    }

    public ResumoLucro resumoLucroPeriodo(String label, LocalDateTime inicio, LocalDateTime fim) {
        DreResumoDto dre = calcularDre(inicio, fim);
        return new ResumoLucro(label, dre.lucroLiquido(), dre.quantidadeServicos());
    }

    /** @deprecated use calcularDre ou resumoLucroPeriodo */
    public ResumoLucro resumoPeriodo(String label, LocalDateTime inicio, LocalDateTime fim) {
        return resumoLucroPeriodo(label, inicio, fim);
    }

    public ResumoLucro resumoCustoPeriodo(String label, LocalDateTime inicio, LocalDateTime fim) {
        BigDecimal total = custoRepository.sumValorBetween(inicio, fim);
        long qtd = custoRepository.countBetween(inicio, fim);
        return new ResumoLucro(label, total, qtd);
    }

    public List<Map<String, Object>> rankingCustos(LocalDateTime inicio, LocalDateTime fim) {
        List<Object[]> rows = custoRepository.sumByDescricaoBetween(inicio, fim);
        List<Map<String, Object>> ranking = new ArrayList<>();
        BigDecimal totalGeral = custoRepository.sumValorBetween(inicio, fim);

        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            BigDecimal total = (BigDecimal) row[1];
            item.put("nome", row[0]);
            item.put("total", total);
            item.put("quantidade", row[2]);
            item.put("percentual", percentualDoTotal(total, totalGeral));
            ranking.add(item);
        }

        return ranking;
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
            item.put("percentual", percentualDoTotal(total, totalGeral));
            ranking.add(item);
        }

        return ranking;
    }

    @Transactional
    public ServicoRealizado registrar(Long tipoServicoId, String clienteNome, String placa,
                                      BigDecimal valor, BigDecimal custoInsumos,
                                      LocalDateTime dataHora, String observacoes) {
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
        servico.setCustoInsumos(custoInsumos != null ? custoInsumos : BigDecimal.ZERO);
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

    @Transactional
    public Custo registrarCusto(String descricao, BigDecimal valor, LocalDateTime dataHora, String observacoes) {
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }

        Custo custo = new Custo();
        custo.setDescricao(descricao.trim());
        custo.setValor(valor);
        custo.setDataHora(dataHora != null ? dataHora : LocalDateTime.now());
        custo.setObservacoes(observacoes != null ? observacoes.trim() : null);

        return custoRepository.save(custo);
    }

    public List<Custo> listarCustosPorPeriodo(LocalDate inicio, LocalDate fim) {
        return custoRepository.findByDataHoraBetweenOrderByDataHoraDesc(
                inicio.atStartOfDay(), fim.atTime(LocalTime.MAX));
    }

    @Transactional
    public void excluirCusto(Long id) {
        if (!custoRepository.existsById(id)) {
            throw new IllegalArgumentException("Custo não encontrado");
        }
        custoRepository.deleteById(id);
    }

    public String gerarCsv(LocalDate inicio, LocalDate fim) {
        List<ServicoRealizado> servicos = listarServicosPorPeriodo(inicio, fim);
        StringBuilder sb = new StringBuilder();
        sb.append("ID;Data;Hora;Servico;Categoria;Cliente;Placa;Valor;CustoInsumos;Observacoes\n");

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
            sb.append(s.getCustoInsumos()).append(';');
            sb.append(escaparCsv(s.getObservacoes())).append('\n');
        }

        return sb.toString();
    }

    private BigDecimal calcularMargem(BigDecimal lucro, BigDecimal receita) {
        if (receita == null || receita.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return lucro.multiply(BigDecimal.valueOf(100))
                .divide(receita, 1, RoundingMode.HALF_UP);
    }

    private BigDecimal percentualDoTotal(BigDecimal parcial, BigDecimal total) {
        if (total.compareTo(BigDecimal.ZERO) > 0) {
            return parcial.multiply(BigDecimal.valueOf(100))
                    .divide(total, 1, RoundingMode.HALF_UP);
        }
        return BigDecimal.ZERO;
    }

    private BigDecimal nullSafe(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }

    private String chavePeriodo(Integer ano, Integer mes, Integer dia, boolean porMes) {
        if (porMes) {
            return String.format("%04d-%02d", ano, mes);
        }
        return String.format("%04d-%02d-%02d", ano, mes, dia);
    }

    private String formatarLabelPeriodo(Integer ano, Integer mes, Integer dia, boolean porMes) {
        if (porMes) {
            return YearMonth.of(ano, mes).format(MES_FORMAT);
        }
        return LocalDate.of(ano, mes, dia).format(DIA_FORMAT);
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

    private static final class LinhaAcumulada {
        private BigDecimal receita = BigDecimal.ZERO;
        private BigDecimal custoInsumos = BigDecimal.ZERO;
        private BigDecimal custosFixos = BigDecimal.ZERO;
        private long quantidadeServicos;
        private Integer ano;
        private Integer mes;
        private Integer dia;
    }
}
