document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('index.html');

    try {
        const data = await NeilanApi.get('/api/dashboard');
        renderResumos(data.resumos);
        renderUltimos(data.ultimosServicos);
    } catch {
        /* redirect handled by api */
    }
});

function renderResumos(resumos) {
    const grid = document.getElementById('cards-grid');
    if (!grid) return;
    grid.innerHTML = resumos.map(r => `
        <div class="card card-gold">
            <div class="card-label">${NeilanUtils.escapeHtml(r.periodo)}</div>
            <div class="card-value">${NeilanUtils.formatMoney(r.total)}</div>
            <div class="card-sub">${r.quantidade} serviço(s)</div>
        </div>
    `).join('');
}

function renderUltimos(servicos) {
    const container = document.getElementById('ultimos-container');
    if (!container) return;

    if (!servicos.length) {
        container.innerHTML = `
            <div class="empty-state">
                <div class="icon">🚗</div>
                <p>Nenhum serviço registrado ainda.</p>
                <a href="registrar.html" class="btn btn-primary" style="margin-top:1rem">Registrar primeiro serviço</a>
            </div>`;
        return;
    }

    container.innerHTML = `
        <div class="table-container">
            <table>
                <thead><tr><th>Data</th><th>Serviço</th><th>Cliente</th><th>Placa</th><th>Valor</th></tr></thead>
                <tbody>${servicos.map(s => `
                    <tr>
                        <td>${NeilanUtils.formatDateTime(s.dataHora)}</td>
                        <td>${NeilanUtils.escapeHtml(s.tipoServicoNome)}</td>
                        <td>${NeilanUtils.escapeHtml(s.clienteNome || '-')}</td>
                        <td>${NeilanUtils.escapeHtml(s.placa || '-')}</td>
                        <td class="valor">${NeilanUtils.formatMoney(s.valor)}</td>
                    </tr>
                `).join('')}</tbody>
            </table>
        </div>`;
}
