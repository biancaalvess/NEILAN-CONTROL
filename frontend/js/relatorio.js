let periodoAtual = 'mensal';

document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('relatorio.html');

    const params = new URLSearchParams(window.location.search);
    periodoAtual = params.get('periodo') || 'mensal';

    document.querySelectorAll('.tab').forEach(tab => {
        tab.classList.toggle('active', tab.dataset.periodo === periodoAtual);
        tab.addEventListener('click', (e) => {
            e.preventDefault();
            periodoAtual = tab.dataset.periodo;
            window.history.replaceState({}, '', `?periodo=${periodoAtual}`);
            document.querySelectorAll('.tab').forEach(t => t.classList.toggle('active', t.dataset.periodo === periodoAtual));
            carregar();
        });
    });

    await carregar();
});

async function carregar() {
    try {
        const data = await NeilanApi.get(`/api/relatorio?periodo=${periodoAtual}`);
        document.getElementById('relatorio-titulo').textContent = data.titulo;

        const ticket = data.resumo.quantidade > 0
            ? NeilanUtils.formatMoney(Number(data.resumo.total) / data.resumo.quantidade)
            : NeilanUtils.formatMoney(0);

        document.getElementById('cards-grid').innerHTML = `
            <div class="card card-gold"><div class="card-label">Lucro Total</div><div class="card-value">${NeilanUtils.formatMoney(data.resumo.total)}</div></div>
            <div class="card card-gold"><div class="card-label">Serviços Realizados</div><div class="card-value">${data.resumo.quantidade}</div></div>
            <div class="card card-gold"><div class="card-label">Ticket Médio</div><div class="card-value">${ticket}</div></div>`;

        renderRanking(data.ranking, data.resumo.total);
        renderTabela(data.servicos);
    } catch { /* */ }
}

function renderRanking(ranking, totalGeral) {
    const el = document.getElementById('ranking-container');
    if (!ranking.length) {
        el.innerHTML = '<div class="empty-state"><p>Sem dados para este período.</p></div>';
        return;
    }

    el.innerHTML = ranking.map(item => {
        const pct = totalGeral > 0 ? (Number(item.total) / Number(totalGeral) * 100).toFixed(1) : 0;
        return `
            <div class="ranking-bar">
                <div class="ranking-bar-header">
                    <span>${NeilanUtils.escapeHtml(item.nome)}</span>
                    <span><strong>${NeilanUtils.formatMoney(item.total)}</strong> (${item.quantidade}x)</span>
                </div>
                <div class="ranking-bar-track"><div class="ranking-bar-fill" style="width:${pct}%"></div></div>
            </div>`;
    }).join('');
}

function renderTabela(servicos) {
    const el = document.getElementById('tabela-container');
    if (!servicos.length) {
        el.innerHTML = '';
        return;
    }

    el.innerHTML = `
        <div class="table-container">
            <table>
                <thead><tr><th>Data</th><th>Serviço</th><th>Cliente</th><th>Valor</th></tr></thead>
                <tbody>${servicos.map(s => `
                    <tr>
                        <td>${NeilanUtils.formatDateTime(s.dataHora)}</td>
                        <td>${NeilanUtils.escapeHtml(s.tipoServicoNome)}</td>
                        <td>${NeilanUtils.escapeHtml(s.clienteNome || '-')}</td>
                        <td class="valor">${NeilanUtils.formatMoney(s.valor)}</td>
                    </tr>
                `).join('')}</tbody>
            </table>
        </div>`;
}
