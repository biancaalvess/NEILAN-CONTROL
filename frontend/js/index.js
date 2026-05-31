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
                <div class="empty-state-icon"></div>
                <p>Nenhum serviço registrado ainda.</p>
                <a href="registrar.html" class="btn btn-primary" style="margin-top:1rem">Registrar primeiro serviço</a>
            </div>`;
        return;
    }

    container.innerHTML = NeilanUtils.renderServicoCards(servicos, { showObs: false });
}
