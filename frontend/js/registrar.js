let tiposServico = [];

document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('registrar.html');

    const select = document.getElementById('tipoServicoId');
    const valorInput = document.getElementById('valor');
    const dataHoraInput = document.getElementById('dataHora');
    const buscaInput = document.getElementById('busca-tipo');

    if (dataHoraInput) dataHoraInput.value = NeilanUtils.nowDatetimeLocal();

    try {
        tiposServico = await NeilanApi.get('/api/tipos-servico?apenasAtivos=true');
        renderTiposSelect();
    } catch { /* */ }

    buscaInput?.addEventListener('input', () => renderTiposSelect(buscaInput.value));

    select?.addEventListener('change', () => {
        const opt = select.options[select.selectedIndex];
        const preco = opt?.dataset.preco;
        if (preco) valorInput.value = Number(preco).toFixed(2);
    });

    document.getElementById('registrar-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const btn = document.getElementById('submit-btn');
        btn.disabled = true;

        const payload = {
            tipoServicoId: Number(select.value),
            clienteNome: document.getElementById('clienteNome').value.trim() || null,
            placa: document.getElementById('placa').value.trim().toUpperCase() || null,
            valor: Number(valorInput.value),
            dataHora: dataHoraInput.value || null,
            observacoes: document.getElementById('observacoes').value.trim() || null
        };

        try {
            await NeilanApi.post('/api/servicos', payload);
            NeilanUtils.showAlert('alert-box', 'Serviço registrado com sucesso!', 'success');
            e.target.reset();
            dataHoraInput.value = NeilanUtils.nowDatetimeLocal();
            if (buscaInput) buscaInput.value = '';
            renderTiposSelect();
        } catch (err) {
            NeilanUtils.showAlert('alert-box', err.message || err.error || 'Erro ao registrar', 'error');
        } finally {
            btn.disabled = false;
        }
    });
});

function renderTiposSelect(termo) {
    const select = document.getElementById('tipoServicoId');
    if (!select) return;

    const filtrados = NeilanUtils.filterTiposServico(tiposServico, termo || '');
    const valorAtual = select.value;

    if (!filtrados.length) {
        select.innerHTML = '<option value="">Nenhum serviço encontrado</option>';
        return;
    }

    select.innerHTML = '<option value="">Selecione o serviço...</option>' +
        filtrados.map(t =>
            `<option value="${t.id}" data-preco="${t.preco}">${NeilanUtils.escapeHtml(t.nome)} — ${NeilanUtils.formatMoney(t.preco)}</option>`
        ).join('');

    if (valorAtual && filtrados.some(t => String(t.id) === valorAtual)) {
        select.value = valorAtual;
    }
}
