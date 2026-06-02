let todosCustos = [];
let totalPeriodoApi = 0;

document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('custos.html');

    const dataHoraInput = document.getElementById('dataHora');
    const inicioInput = document.getElementById('inicio');
    const fimInput = document.getElementById('fim');
    const buscaInput = document.getElementById('busca-custo');

    if (dataHoraInput) dataHoraInput.value = NeilanUtils.nowDatetimeLocal();
    inicioInput.value = NeilanUtils.monthStartInput();
    fimInput.value = NeilanUtils.todayInput();

    document.getElementById('custo-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('submit-btn');
        btn.disabled = true;

        const payload = {
            descricao: document.getElementById('descricao').value.trim(),
            valor: Number(document.getElementById('valor').value),
            dataHora: dataHoraInput.value || null,
            observacoes: document.getElementById('observacoes').value.trim() || null
        };

        try {
            await NeilanApi.post('/api/custos', payload);
            NeilanUtils.showAlert('alert-box', 'Custo lançado com sucesso!', 'success');
            e.target.reset();
            dataHoraInput.value = NeilanUtils.nowDatetimeLocal();
            await carregar();
        } catch (err) {
            NeilanUtils.showAlert('alert-box', err.message || err.error || 'Erro ao lançar custo', 'error');
        } finally {
            btn.disabled = false;
        }
    });

    document.getElementById('filtro-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        carregar();
    });

    buscaInput?.addEventListener('input', () => renderLista());

    await carregar();
});

async function carregar() {
    const inicio = document.getElementById('inicio').value;
    const fim = document.getElementById('fim').value;
    const container = document.getElementById('custos-container');

    container.innerHTML = '<div class="empty-state"><p>Carregando...</p></div>';
    renderDashboard([], 0);

    try {
        const data = await NeilanApi.get(`/api/custos?inicio=${inicio}&fim=${fim}`);
        todosCustos = data.custos || [];
        totalPeriodoApi = Number(data.totalPeriodo || 0);
        renderLista();
    } catch (err) {
        todosCustos = [];
        totalPeriodoApi = 0;
        renderDashboard([], 0);
        container.innerHTML = `<div class="empty-state"><p>${NeilanUtils.escapeHtml(err.message || err.error || 'Erro ao carregar custos. Verifique se o backend está rodando.')}</p></div>`;
        document.getElementById('summary-bar').innerHTML = '';
    }
}

function renderDashboard(custos, total) {
    const el = document.getElementById('expense-dashboard-content');
    if (!el) return;
    el.innerHTML = NeilanUtils.renderExpenseDashboard(custos, total);
}

function renderLista() {
    const busca = document.getElementById('busca-custo')?.value || '';
    const filtrados = filterCustos(todosCustos, busca);

    const total = busca.trim()
        ? filtrados.reduce((sum, c) => sum + Number(c.valor || 0), 0)
        : totalPeriodoApi;
    const qtd = filtrados.length;

    renderDashboard(filtrados, total);

    document.getElementById('summary-bar').innerHTML = `
        <div class="summary-item"><span class="summary-label">Custos</span><strong>${qtd}</strong></div>
        <div class="summary-item summary-item--expense"><span class="summary-label">Total saída</span><strong>${NeilanUtils.formatMoney(total)}</strong></div>`;

    const container = document.getElementById('custos-container');
    if (!filtrados.length) {
        const msg = busca.trim()
            ? 'Nenhum custo encontrado para esta busca.'
            : 'Nenhum custo registrado neste período.';
        container.innerHTML = `<div class="empty-state"><div class="empty-state-icon"></div><p>${msg}</p></div>`;
        return;
    }

    container.innerHTML = NeilanUtils.renderCustoCards(filtrados);

    container.querySelectorAll('.btn-delete-custo').forEach(btn => {
        btn.addEventListener('click', async () => {
            if (!confirm('Excluir este custo?')) return;
            try {
                await NeilanApi.delete(`/api/custos/${btn.dataset.id}`);
                await carregar();
            } catch {
                alert('Erro ao excluir custo');
            }
        });
    });
}

function filterCustos(custos, term) {
    const q = NeilanUtils.normalizeSearch(term.trim());
    if (!q) return custos;
    return custos.filter(c => {
        const haystack = [c.descricao, c.observacoes, c.valor != null ? String(c.valor) : ''].join(' ');
        return NeilanUtils.normalizeSearch(haystack).includes(q);
    });
}
