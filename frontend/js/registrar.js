document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('registrar.html');

    const select = document.getElementById('tipoServicoId');
    const valorInput = document.getElementById('valor');
    const dataHoraInput = document.getElementById('dataHora');

    if (dataHoraInput) dataHoraInput.value = NeilanUtils.nowDatetimeLocal();

    try {
        const tipos = await NeilanApi.get('/api/tipos-servico?apenasAtivos=true');
        select.innerHTML = '<option value="">Selecione o serviço...</option>' +
            tipos.map(t => `<option value="${t.id}" data-preco="${t.preco}">${NeilanUtils.escapeHtml(t.nome)} — ${NeilanUtils.formatMoney(t.preco)}</option>`).join('');
    } catch { /* */ }

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
        } catch (err) {
            NeilanUtils.showAlert('alert-box', err.message || err.error || 'Erro ao registrar', 'error');
        } finally {
            btn.disabled = false;
        }
    });
});
