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
    }
};
