let periodoAtual = 'mensal';

document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('relatorio.html');

    const params = new URLSearchParams(window.location.search);
    periodoAtual = params.get('periodo') || 'mensal';

    document.querySelectorAll('#periodo-tabs .tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.periodo === periodoAtual);
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            periodoAtual = tab.dataset.periodo;
            syncUrl();
            syncTabs();
            carregar();
        });
    });

    await carregar();
});

function syncUrl() {
    window.history.replaceState({}, '', `?periodo=${periodoAtual}`);
}

function syncTabs() {
    document.querySelectorAll('#periodo-tabs .tab').forEach(t =>
        t.classList.toggle('active', t.dataset.periodo === periodoAtual));
}

async function carregar() {
    syncTabs();

    try {
        const data = await NeilanApi.get(`/api/relatorio/financeiro?periodo=${periodoAtual}`);
        document.getElementById('relatorio-titulo').textContent = data.titulo;
        document.getElementById('relatorio-subtitulo').textContent =
            'DRE simplificada — receita, custos e lucro líquido no período';

        renderDre(data);
        renderRanking(data.rankingServicos, data.dre.receitaBruta, false, 'ranking-servicos');
        renderRanking(data.rankingCustos, data.dre.custosFixosVariaveis, true, 'ranking-custos');
    } catch (err) {
        NeilanUtils.showAlert('alert-box', err.message || err.error || 'Erro ao carregar relatório', 'error');
    }
}

function renderDre(data) {
    const dre = data.dre;
    const margem = Number(dre.margemPercentual || 0);
    const margemClass = margem >= 0 ? '' : 'card-value--expense';

    document.getElementById('cards-grid').innerHTML = `
        <div class="card card-gold">
            <div class="card-label">Receita Bruta</div>
            <div class="card-value">${NeilanUtils.formatMoney(dre.receitaBruta)}</div>
        </div>
        <div class="card card-expense">
            <div class="card-label">Custos Totais</div>
            <div class="card-value card-value--expense">${NeilanUtils.formatMoney(dre.custosTotais)}</div>
        </div>
        <div class="card card-gold">
            <div class="card-label">Lucro Líquido</div>
            <div class="card-value ${Number(dre.lucroLiquido) < 0 ? 'card-value--expense' : ''}">${NeilanUtils.formatMoney(dre.lucroLiquido)}</div>
        </div>
        <div class="card card-gold card--full-mobile">
            <div class="card-label">Margem %</div>
            <div class="card-value ${margemClass}">${margem.toFixed(1)}%</div>
        </div>`;

    document.getElementById('dre-detalhes').innerHTML = `
        <div class="summary-bar summary-bar--dre">
            <div class="summary-item">
                <span class="summary-label">Custo operacional direto (insumos)</span>
                <strong class="text-expense">${NeilanUtils.formatMoney(dre.custoOperacionalDireto)}</strong>
            </div>
            <div class="summary-item">
                <span class="summary-label">Custos fixos / variáveis</span>
                <strong class="text-expense">${NeilanUtils.formatMoney(dre.custosFixosVariaveis)}</strong>
            </div>
            <div class="summary-item">
                <span class="summary-label">Serviços no período</span>
                <strong>${dre.quantidadeServicos}</strong>
            </div>
            <div class="summary-item">
                <span class="summary-label">Lançamentos de custo</span>
                <strong>${dre.quantidadeCustos}</strong>
            </div>
        </div>`;

    renderTabelaDre(data.linhas || []);
}

function renderTabelaDre(linhas) {
    const el = document.getElementById('tabela-dre');
    if (!linhas.length) {
        el.innerHTML = '<div class="empty-state"><p>Sem movimentação financeira neste período.</p></div>';
        return;
    }

    el.innerHTML = `
        <div class="table-responsive">
            <table class="data-table data-table--dre">
                <thead>
                    <tr>
                        <th>Período</th>
                        <th>Receita</th>
                        <th>Insumos</th>
                        <th>Custos fixos/var.</th>
                        <th>Custos totais</th>
                        <th>Lucro</th>
                        <th>Margem</th>
                        <th>Serviços</th>
                    </tr>
                </thead>
                <tbody>
                    ${linhas.map(l => {
                        const lucroNegativo = Number(l.lucroLiquido) < 0;
                        return `
                        <tr>
                            <td>${NeilanUtils.escapeHtml(l.periodoLabel)}</td>
                            <td>${NeilanUtils.formatMoney(l.receitaBruta)}</td>
                            <td class="text-expense">${NeilanUtils.formatMoney(l.custoOperacionalDireto)}</td>
                            <td class="text-expense">${NeilanUtils.formatMoney(l.custosFixosVariaveis)}</td>
                            <td class="text-expense">${NeilanUtils.formatMoney(l.custosTotais)}</td>
                            <td class="${lucroNegativo ? 'text-expense' : ''}">${NeilanUtils.formatMoney(l.lucroLiquido)}</td>
                            <td>${Number(l.margemPercentual || 0).toFixed(1)}%</td>
                            <td>${l.quantidadeServicos}</td>
                        </tr>`;
                    }).join('')}
                </tbody>
            </table>
        </div>`;
}

function renderRanking(ranking, totalGeral, isExpense, containerId) {
    const el = document.getElementById(containerId);
    const vazio = isExpense ? 'Sem custos neste período.' : 'Sem serviços neste período.';

    if (!ranking?.length) {
        el.innerHTML = `<div class="empty-state"><p>${vazio}</p></div>`;
        return;
    }

    el.innerHTML = ranking.map(item => {
        const pct = totalGeral > 0 ? (Number(item.total) / Number(totalGeral) * 100).toFixed(1) : 0;
        const barClass = isExpense ? 'ranking-bar ranking-bar--expense' : 'ranking-bar';
        const fillClass = isExpense ? 'ranking-bar-fill ranking-bar-fill--expense' : 'ranking-bar-fill';
        const valueClass = isExpense ? 'text-expense' : '';
        return `
            <div class="${barClass}">
                <div class="ranking-bar-header">
                    <span>${NeilanUtils.escapeHtml(item.nome)}</span>
                    <span><strong class="${valueClass}">${NeilanUtils.formatMoney(item.total)}</strong> (${item.quantidade}x)</span>
                </div>
                <div class="ranking-bar-track"><div class="${fillClass}" style="width:${pct}%"></div></div>
            </div>`;
    }).join('');
}
