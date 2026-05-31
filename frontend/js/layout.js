const NAV_ITEMS = [
    { href: 'index.html', label: 'Dashboard' },
    { href: 'registrar.html', label: 'Registrar Serviço' },
    { href: 'servicos.html', label: 'Serviços Feitos' },
    { href: 'relatorio.html', label: 'Relatório' },
    { href: 'configuracao.html', label: 'Configuração' }
];

const PAGE_TITLES = {
    'index.html': 'Dashboard',
    'registrar.html': 'Registrar',
    'servicos.html': 'Serviços',
    'relatorio.html': 'Relatório',
    'configuracao.html': 'Configuração'
};

const BOTTOM_NAV = [
    { href: 'index.html', label: 'Início' },
    { href: 'registrar.html', label: 'Registrar' },
    { href: 'servicos.html', label: 'Histórico' },
    { href: 'relatorio.html', label: 'Relatório' },
    { href: 'configuracao.html', label: 'Config' }
];

function closeSidebar() {
    document.getElementById('sidebar')?.classList.remove('open');
    document.getElementById('sidebar-overlay')?.classList.remove('visible');
    document.body.classList.remove('menu-open');
}

function openSidebar() {
    document.getElementById('sidebar')?.classList.add('open');
    document.getElementById('sidebar-overlay')?.classList.add('visible');
    document.body.classList.add('menu-open');
}

async function logoutApp() {
    try {
        await NeilanApi.logout();
    } catch {
        /* segue para login mesmo se a API falhar */
    }
    NeilanTransitions.navigate('login.html');
}

function bindLogoutButton(id) {
    document.getElementById(id)?.addEventListener('click', logoutApp);
}

async function initLayout(activePath) {
    const user = await NeilanApi.me();
    if (!user) {
        NeilanTransitions.navigate('login.html');
        return null;
    }

    const pageTitle = PAGE_TITLES[activePath] || 'Neilan Control';

    const mobileHeader = document.getElementById('mobile-header');
    if (mobileHeader) {
        mobileHeader.innerHTML = `
            <button type="button" class="mobile-header-btn mobile-header-btn--menu" id="menu-open-btn" aria-label="Abrir menu">
                <span></span><span></span><span></span>
            </button>
            <div class="mobile-header-title">
                <img src="public/logo.png" alt="" class="mobile-header-logo">
                <span>${NeilanUtils.escapeHtml(pageTitle)}</span>
            </div>
            <button type="button" class="mobile-header-btn mobile-header-btn--logout" id="header-logout-btn">Sair</button>`;
    }

    const bottomNav = document.getElementById('bottom-nav');
    if (bottomNav) {
        bottomNav.innerHTML = BOTTOM_NAV.map(item => `
            <a href="${item.href}" class="bottom-nav-item ${activePath === item.href ? 'active' : ''}">
                <span class="bottom-nav-label">${item.label}</span>
            </a>
        `).join('');
    }

    const sidebar = document.getElementById('sidebar');
    if (sidebar) {
        const navHtml = NAV_ITEMS.map(item => {
            const isActive = activePath === item.href;
            return `<a href="${item.href}" class="sidebar-link ${isActive ? 'active' : ''}">${item.label}</a>`;
        }).join('');

        sidebar.innerHTML = `
            <div class="sidebar-header">
                <img src="public/logo.png" alt="Neilan Estética Automotiva" class="brand-logo brand-logo--sidebar">
                <button type="button" class="sidebar-close" id="sidebar-close" aria-label="Fechar menu">&times;</button>
            </div>
            <nav class="sidebar-nav">${navHtml}
                <a href="#" id="export-link" class="sidebar-link">Exportar CSV</a>
            </nav>
            <div class="sidebar-footer">
                <div class="user-info">
                    <span class="user-label">Logado como</span>
                    <span class="user-email">${NeilanUtils.escapeHtml(user.email)}</span>
                </div>
                <button type="button" id="logout-btn" class="btn btn-secondary btn-sm logout-btn">Sair</button>
            </div>`;

        sidebar.querySelectorAll('.sidebar-link[href]').forEach(link => {
            if (link.getAttribute('href') !== '#') {
                link.addEventListener('click', () => closeSidebar());
            }
        });
    }

    document.getElementById('menu-open-btn')?.addEventListener('click', openSidebar);
    document.getElementById('sidebar-close')?.addEventListener('click', closeSidebar);
    document.getElementById('sidebar-overlay')?.addEventListener('click', closeSidebar);

    bindLogoutButton('logout-btn');
    bindLogoutButton('header-logout-btn');

    document.getElementById('export-link')?.addEventListener('click', async (e) => {
        e.preventDefault();
        closeSidebar();
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

    return user;
}
