let tipoAtual = 'servicos';
let periodoAtual = 'mensal';

document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('relatorio.html');

    const params = new URLSearchParams(window.location.search);
    tipoAtual = params.get('tipo') === 'custos' ? 'custos' : 'servicos';
    periodoAtual = params.get('periodo') || 'mensal';

    document.querySelectorAll('#tipo-tabs .tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.tipo === tipoAtual);
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            tipoAtual = tab.dataset.tipo;
            syncUrl();
            syncTabs();
            carregar();
        });
    });

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
    window.history.replaceState({}, '', `?tipo=${tipoAtual}&periodo=${periodoAtual}`);
}

function syncTabs() {
    document.querySelectorAll('#tipo-tabs .tab').forEach(t =>
        t.classList.toggle('active', t.dataset.tipo === tipoAtual));
    document.querySelectorAll('#periodo-tabs .tab').forEach(t =>
        t.classList.toggle('active', t.dataset.periodo === periodoAtual));
    document.getElementById('ranking-titulo').textContent =
        tipoAtual === 'custos' ? 'Ranking por Descrição' : 'Ranking por Serviço';
}

async function carregar() {
    syncTabs();
    const endpoint = tipoAtual === 'custos'
        ? `/api/relatorio/custos?periodo=${periodoAtual}`
        : `/api/relatorio/servicos?periodo=${periodoAtual}`;

    try {
        const data = await NeilanApi.get(endpoint);
        document.getElementById('relatorio-titulo').textContent = data.titulo;
        document.getElementById('relatorio-subtitulo').textContent =
            tipoAtual === 'custos'
                ? 'Despesas e gastos no período selecionado'
                : 'Lucro e desempenho no período selecionado';

        if (tipoAtual === 'custos') {
            renderCustos(data);
        } else {
            renderServicos(data);
        }
    } catch (err) {
        NeilanUtils.showAlert('alert-box', err.message || err.error || 'Erro ao carregar relatório', 'error');
    }
}

function renderServicos(data) {
    const ticket = data.resumo.quantidade > 0
        ? NeilanUtils.formatMoney(Number(data.resumo.total) / data.resumo.quantidade)
        : NeilanUtils.formatMoney(0);

    document.getElementById('cards-grid').innerHTML = `
        <div class="card card-gold"><div class="card-label">Lucro Total</div><div class="card-value">${NeilanUtils.formatMoney(data.resumo.total)}</div></div>
        <div class="card card-gold"><div class="card-label">Serviços</div><div class="card-value">${data.resumo.quantidade}</div></div>
        <div class="card card-gold card--full-mobile"><div class="card-label">Ticket Médio</div><div class="card-value">${ticket}</div></div>`;

    renderRanking(data.ranking, data.resumo.total, false);

    const el = document.getElementById('tabela-container');
    el.innerHTML = data.servicos?.length
        ? NeilanUtils.renderServicoCards(data.servicos, { showObs: false })
        : '';
}

function renderCustos(data) {
    const media = data.resumo.quantidade > 0
        ? NeilanUtils.formatMoney(Number(data.resumo.total) / data.resumo.quantidade)
        : NeilanUtils.formatMoney(0);

    document.getElementById('cards-grid').innerHTML = `
        <div class="card card-expense"><div class="card-label">Total Saídas</div><div class="card-value card-value--expense">${NeilanUtils.formatMoney(data.resumo.total)}</div></div>
        <div class="card card-expense"><div class="card-label">Lançamentos</div><div class="card-value card-value--expense">${data.resumo.quantidade}</div></div>
        <div class="card card-expense card--full-mobile"><div class="card-label">Média por Lançamento</div><div class="card-value card-value--expense">${media}</div></div>`;

    renderRanking(data.ranking, data.resumo.total, true);

    const el = document.getElementById('tabela-container');
    el.innerHTML = data.custos?.length
        ? NeilanUtils.renderCustoCards(data.custos, { showDelete: false })
        : '';
}

function renderRanking(ranking, totalGeral, isExpense) {
    const el = document.getElementById('ranking-container');
    const vazio = isExpense ? 'Sem custos neste período.' : 'Sem serviços neste período.';

    if (!ranking.length) {
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
