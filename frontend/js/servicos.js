let todosServicos = [];

document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('servicos.html');

    const inicioInput = document.getElementById('inicio');
    const fimInput = document.getElementById('fim');
    const buscaInput = document.getElementById('busca-servico');
    inicioInput.value = NeilanUtils.monthStartInput();
    fimInput.value = NeilanUtils.todayInput();

    document.getElementById('filtro-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        carregar();
    });

    buscaInput?.addEventListener('input', () => renderLista());

    document.getElementById('export-btn')?.addEventListener('click', async () => {
        try {
            const blob = await NeilanApi.downloadCsv(inicioInput.value, fimInput.value);
            const a = document.createElement('a');
            a.href = URL.createObjectURL(blob);
            a.download = `neilan-${inicioInput.value}-${fimInput.value}.csv`;
            a.click();
        } catch {
            alert('Erro ao exportar');
        }
    });

    await carregar();
});

async function carregar() {
    const inicio = document.getElementById('inicio').value;
    const fim = document.getElementById('fim').value;

    try {
        const data = await NeilanApi.get(`/api/servicos?inicio=${inicio}&fim=${fim}`);
        todosServicos = data.servicos || [];
        renderLista();
    } catch { /* */ }
}

function renderLista() {
    const busca = document.getElementById('busca-servico')?.value || '';
    const filtrados = NeilanUtils.filterServicos(todosServicos, busca);

    const total = filtrados.reduce((sum, s) => sum + Number(s.valor || 0), 0);
    const qtd = filtrados.length;

    document.getElementById('summary-bar').innerHTML = `
        <div class="summary-item"><span class="summary-label">Serviços</span><strong>${qtd}</strong></div>
        <div class="summary-item"><span class="summary-label">Total</span><strong>${NeilanUtils.formatMoney(total)}</strong></div>`;

    const container = document.getElementById('servicos-container');
    if (!filtrados.length) {
        const msg = busca.trim()
            ? 'Nenhum serviço encontrado para esta busca.'
            : 'Nenhum serviço encontrado neste período.';
        container.innerHTML = `<div class="empty-state"><div class="empty-state-icon"></div><p>${msg}</p></div>`;
        return;
    }

    container.innerHTML = NeilanUtils.renderServicoCards(filtrados);
}
