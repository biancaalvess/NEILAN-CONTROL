const NeilanTransitions = (() => {
    document.documentElement.classList.add('transitions-enabled');

    const DURATION = 160;
    const PAGES = new Set([
        'index.html',
        'login.html',
        'registrar.html',
        'servicos.html',
        'custos.html',
        'custo.html',
        'relatorio.html',
        'relatorio-servicos.html',
        'relatorio-custos.html',
        'configuracao.html'
    ]);

    let navigating = false;

    function prefersReducedMotion() {
        return window.matchMedia('(prefers-reduced-motion: reduce)').matches;
    }

    function pageName(pathname) {
        const name = pathname.split('/').pop();
        return name || 'index.html';
    }

    function isInternalPage(href) {
        try {
            const url = new URL(href, window.location.href);
            if (url.origin !== window.location.origin) return false;
            return PAGES.has(pageName(url.pathname));
        } catch {
            return false;
        }
    }

    function goTo(url, replace) {
        if (replace) window.location.replace(url);
        else window.location.assign(url);
    }

    function navigate(url, { replace = false } = {}) {
        if (navigating) return;
        navigating = true;

        if (prefersReducedMotion() || document.body.classList.contains('page-leaving')) {
            goTo(url, replace);
            return;
        }

        document.body.classList.add('page-leaving');
        window.setTimeout(() => goTo(url, replace), DURATION);
    }

    function initEnter() {
        if (prefersReducedMotion()) {
            document.body.classList.add('page-ready');
            return;
        }
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                document.body.classList.add('page-ready');
            });
        });
    }

    function bindLinks() {
        document.addEventListener('click', (e) => {
            if (e.defaultPrevented || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) return;
            const link = e.target.closest('a[href]');
            if (!link || link.target === '_blank' || link.hasAttribute('download')) return;

            const href = link.getAttribute('href');
            if (!href || href === '#' || href.startsWith('javascript:')) return;
            if (!isInternalPage(href)) return;

            const url = new URL(href, window.location.href);
            const samePage = pageName(url.pathname) === pageName(window.location.pathname)
                && url.search === window.location.search
                && !url.hash;
            if (samePage) {
                e.preventDefault();
                return;
            }

            e.preventDefault();
            navigate(url.pathname + url.search + url.hash);
        }, { capture: true });
    }

    function init() {
        initEnter();
        bindLinks();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', init);
    } else {
        init();
    }

    return { navigate, DURATION };
})();
