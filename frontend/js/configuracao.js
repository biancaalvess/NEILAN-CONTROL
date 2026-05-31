document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('configuracao.html');
    await carregarTipos();

    document.getElementById('novo-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const payload = {
            nome: document.getElementById('nome').value.trim(),
            categoria: document.getElementById('categoria').value,
            descricao: document.getElementById('descricao').value.trim(),
            preco: Number(document.getElementById('preco').value),
            ativo: true
        };

        try {
            await NeilanApi.post('/api/tipos-servico', payload);
            NeilanUtils.showAlert('alert-box', 'Serviço adicionado!', 'success');
            e.target.reset();
            await carregarTipos();
        } catch (err) {
            NeilanUtils.showAlert('alert-box', err.message || 'Erro ao adicionar', 'error');
        }
    });
});

async function carregarTipos() {
    try {
        const tipos = await NeilanApi.get('/api/tipos-servico');
        const container = document.getElementById('tipos-container');

        if (!tipos.length) {
            container.innerHTML = '<div class="empty-state"><p>Nenhum serviço configurado.</p></div>';
            return;
        }

        container.innerHTML = tipos.map(t => `
            <div class="config-item" data-id="${t.id}">
                <div class="config-item-header">
                    <h4 class="config-item-title">${NeilanUtils.escapeHtml(t.nome)}</h4>
                    <span class="badge ${t.ativo ? 'badge-ativo' : 'badge-inativo'}">${t.ativo ? 'Ativo' : 'Inativo'}</span>
                </div>
                <div class="config-form-grid">
                    <div class="config-field config-field--nome">
                        <label>Nome</label>
                        <input type="text" class="edit-nome" value="${NeilanUtils.escapeHtml(t.nome)}" autocomplete="off">
                    </div>
                    <div class="config-field config-field--categoria">
                        <label>Categoria</label>
                        <select class="edit-categoria">${categoriaOptions(t.categoria)}</select>
                    </div>
                    <div class="config-form-row">
                        <div class="config-field config-field--preco">
                            <label>Preço</label>
                            <div class="input-currency">
                                <span class="input-currency-prefix" aria-hidden="true">R$</span>
                                <input type="number" class="edit-preco" step="0.01" min="0.01" value="${t.preco}" inputmode="decimal">
                            </div>
                        </div>
                        <div class="config-field config-field--status">
                            <label>Status</label>
                            <label class="toggle-switch" aria-label="Serviço ativo">
                                <input type="checkbox" class="edit-ativo" ${t.ativo ? 'checked' : ''}>
                                <span class="toggle-slider"></span>
                            </label>
                        </div>
                    </div>
                    <div class="config-actions">
                        <button type="button" class="btn btn-danger btn-excluir">Excluir</button>
                        <button type="button" class="btn btn-primary btn-salvar">Salvar</button>
                    </div>
                </div>
            </div>
        `).join('');

        container.querySelectorAll('.btn-salvar').forEach(btn => {
            btn.addEventListener('click', () => salvarItem(btn.closest('.config-item')));
        });
        container.querySelectorAll('.btn-excluir').forEach(btn => {
            btn.addEventListener('click', () => excluirItem(btn.closest('.config-item')));
        });
    } catch { /* */ }
}

function categoriaOptions(selected) {
    const cats = ['Estética Automotiva', 'Lavagem de Estofados', 'Lavagens em Geral'];
    return cats.map(c => `<option value="${c}" ${c === selected ? 'selected' : ''}>${c}</option>`).join('');
}

async function salvarItem(item) {
    const id = item.dataset.id;
    const payload = {
        nome: item.querySelector('.edit-nome').value.trim(),
        categoria: item.querySelector('.edit-categoria').value,
        descricao: '',
        preco: Number(item.querySelector('.edit-preco').value),
        ativo: item.querySelector('.edit-ativo').checked
    };

    try {
        await NeilanApi.put(`/api/tipos-servico/${id}`, payload);
        NeilanUtils.showAlert('alert-box', 'Serviço atualizado!', 'success');
        await carregarTipos();
    } catch (err) {
        NeilanUtils.showAlert('alert-box', err.message || 'Erro ao salvar', 'error');
    }
}

async function excluirItem(item) {
    if (!confirm('Remover este serviço?')) return;
    const id = item.dataset.id;
    try {
        await NeilanApi.delete(`/api/tipos-servico/${id}`);
        NeilanUtils.showAlert('alert-box', 'Serviço removido!', 'success');
        await carregarTipos();
    } catch {
        NeilanUtils.showAlert('alert-box', 'Erro ao excluir', 'error');
    }
}
