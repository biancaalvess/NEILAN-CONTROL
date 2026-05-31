const NAV_ITEMS = [
    { href: 'index.html', icon: '📊', label: 'Dashboard' },
    { href: 'registrar.html', icon: '➕', label: 'Registrar Serviço' },
    { href: 'servicos.html', icon: '📋', label: 'Serviços Feitos' },
    { href: 'relatorio.html', icon: '📈', label: 'Relatório' },
    { href: 'configuracao.html', icon: '⚙️', label: 'Configuração' }
];

async function initLayout(activePath) {
    const user = await NeilanApi.me();
    if (!user) {
        window.location.href = 'login.html';
        return null;
    }

    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        const navHtml = NAV_ITEMS.map(item => {
            const isActive = activePath === item.href;
            return `<a href="${item.href}" class="${isActive ? 'active' : ''}"><span class="icon">${item.icon}</span> ${item.label}</a>`;
        }).join('');

        sidebar.innerHTML = `
            <div class="sidebar-brand">
                <img src="public/logo.png" alt="Neilan Estética Automotiva" class="brand-logo brand-logo--sidebar">
            </div>
            <nav class="sidebar-nav">${navHtml}
                <a href="#" id="export-link"><span class="icon">📥</span> Exportar CSV</a>
            </nav>
            <div class="sidebar-footer">
                <div class="user-info">
                    <span class="user-label">Logado como</span>
                    <span class="user-email">${NeilanUtils.escapeHtml(user.email)}</span>
                </div>
                <button type="button" id="logout-btn" class="btn btn-secondary btn-sm logout-btn">Sair</button>
            </div>`;
    }

    document.getElementById('logout-btn')?.addEventListener('click', async () => {
        await NeilanApi.logout();
        window.location.href = 'login.html';
    });

    document.getElementById('export-link')?.addEventListener('click', async (e) => {
        e.preventDefault();
        try {
            const blob = await NeilanApi.downloadCsv(NeilanUtils.monthStartInput(), NeilanUtils.todayInput());
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'neilan-servicos.csv';
            a.click();
            URL.revokeObjectURL(url);
        } catch {
            alert('Erro ao exportar CSV');
        }
    });

    const toggle = document.querySelector('.menu-toggle');
    const sidebarEl = document.getElementById('sidebar');
    toggle?.addEventListener('click', () => sidebarEl?.classList.toggle('open'));

    return user;
}
