const NeilanUtils = {
    formatMoney(value) {
        const num = Number(value) || 0;
        return num.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
    },

    formatDateTime(iso) {
        if (!iso) return '-';
        const d = new Date(iso);
        return d.toLocaleString('pt-BR', {
            day: '2-digit', month: '2-digit', year: 'numeric',
            hour: '2-digit', minute: '2-digit'
        });
    },

    formatDateInput(date) {
        return date.toISOString().split('T')[0];
    },

    todayInput() {
        return this.formatDateInput(new Date());
    },

    monthStartInput() {
        const d = new Date();
        d.setDate(1);
        return this.formatDateInput(d);
    },

    nowDatetimeLocal() {
        const d = new Date();
        d.setMinutes(d.getMinutes() - d.getTimezoneOffset());
        return d.toISOString().slice(0, 16);
    },

    showAlert(containerId, message, type = 'success') {
        const el = document.getElementById(containerId);
        if (!el) return;
        el.innerHTML = `<div class="alert alert-${type}">${message}</div>`;
        setTimeout(() => { el.innerHTML = ''; }, 4000);
    },

    escapeHtml(text) {
        if (text == null) return '';
        const div = document.createElement('div');
        div.textContent = text;
        return div.innerHTML;
    },

    normalizeSearch(text) {
        return (text || '').toLowerCase()
            .normalize('NFD')
            .replace(/[\u0300-\u036f]/g, '');
    },

    filterServicos(servicos, term) {
        const q = this.normalizeSearch(term.trim());
        if (!q) return servicos;
        return servicos.filter(s => {
            const haystack = [
                s.tipoServicoNome,
                s.categoria,
                s.clienteNome,
                s.placa,
                s.observacoes,
                s.valor != null ? String(s.valor) : ''
            ].join(' ');
            return this.normalizeSearch(haystack).includes(q);
        });
    },

    filterTiposServico(tipos, term) {
        const q = this.normalizeSearch(term.trim());
        if (!q) return tipos;
        return tipos.filter(t => {
            const haystack = [t.nome, t.categoria, t.descricao, String(t.preco)].join(' ');
            return this.normalizeSearch(haystack).includes(q);
        });
    },

    renderServicoCards(servicos, options = {}) {
        const showObs = options.showObs !== false;
        return `<div class="service-cards">${servicos.map(s => `
            <article class="service-card">
                <div class="service-card-top">
                    <div>
                        <div class="service-card-title">${this.escapeHtml(s.tipoServicoNome)}</div>
                        <div class="service-card-date">${this.formatDateTime(s.dataHora)}</div>
                    </div>
                    <div class="service-card-value">${this.formatMoney(s.valor)}</div>
                </div>
                <div class="service-card-meta">
                    ${s.categoria ? `<span class="badge badge-categoria">${this.escapeHtml(s.categoria)}</span>` : ''}
                    ${s.placa ? `<span class="service-card-tag">Placa: ${this.escapeHtml(s.placa)}</span>` : ''}
                    ${s.clienteNome ? `<span class="service-card-tag">${this.escapeHtml(s.clienteNome)}</span>` : ''}
                </div>
                ${showObs && s.observacoes ? `<p class="service-card-obs">${this.escapeHtml(s.observacoes)}</p>` : ''}
            </article>
        `).join('')}</div>`;
    },

    renderCustoCards(custos, options = {}) {
        const showDelete = options.showDelete !== false;
        return `<div class="service-cards">${custos.map(c => `
            <article class="service-card service-card--expense">
                <div class="service-card-top">
                    <div>
                        <div class="service-card-title">${this.escapeHtml(c.descricao)}</div>
                        <div class="service-card-date">${this.formatDateTime(c.dataHora)}</div>
                    </div>
                    <div class="service-card-value service-card-value--expense">− ${this.formatMoney(c.valor)}</div>
                </div>
                ${c.observacoes ? `<p class="service-card-obs">${this.escapeHtml(c.observacoes)}</p>` : ''}
                ${showDelete ? `<button type="button" class="btn btn-secondary btn-sm btn-delete-custo" data-id="${c.id}">Excluir</button>` : ''}
            </article>
        `).join('')}</div>`;
    },

    aggregateCustosByDescricao(custos) {
        const map = new Map();
        for (const c of custos) {
            const key = (c.descricao || 'Sem descrição').trim();
            const val = Number(c.valor) || 0;
            const prev = map.get(key) || { nome: key, total: 0, quantidade: 0 };
            prev.total += val;
            prev.quantidade += 1;
            map.set(key, prev);
        }
        return [...map.values()].sort((a, b) => b.total - a.total);
    },

    renderExpenseDashboard(custos, totalGeral) {
        const total = Number(totalGeral) || 0;
        const qtd = custos.length;
        const agregados = this.aggregateCustosByDescricao(custos);

        const metaHtml = `
            <p class="expense-dashboard-meta">
                <strong>${this.formatMoney(total)}</strong>
                <span class="expense-dashboard-sep">·</span>
                ${qtd} ${qtd === 1 ? 'lançamento' : 'lançamentos'}
            </p>`;

        if (!qtd || total <= 0) {
            return `${metaHtml}<p class="expense-dashboard-empty">Sem gastos no período.</p>`;
        }

        const barsHtml = agregados.slice(0, 5).map(item => {
            const pct = total > 0 ? (item.total / total * 100) : 0;
            return `
                <div class="expense-mini-bar">
                    <div class="expense-mini-bar-row">
                        <span class="expense-mini-bar-name">${this.escapeHtml(item.nome)}</span>
                        <span class="expense-mini-bar-value">${this.formatMoney(item.total)}</span>
                    </div>
                    <div class="expense-mini-bar-track">
                        <div class="expense-mini-bar-fill" style="width:${pct.toFixed(1)}%"></div>
                    </div>
                </div>`;
        }).join('');

        return `${metaHtml}<div class="expense-mini-bars">${barsHtml}</div>`;
    }
};
