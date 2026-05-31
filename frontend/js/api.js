const NeilanApi = (() => {
    function getCsrfToken() {
        const match = document.cookie.match(/XSRF-TOKEN=([^;]+)/);
        return match ? decodeURIComponent(match[1]) : null;
    }

    async function ensureCsrf() {
        if (!getCsrfToken()) {
            await fetch(NeilanConfig.api('/api/auth/me'), { credentials: 'include' });
        }
    }

    async function request(url, options = {}) {
        await ensureCsrf();
        const method = (options.method || 'GET').toUpperCase();
        const headers = { ...(options.headers || {}) };
        let body = options.body;

        if (body && typeof body === 'object' && !(body instanceof FormData) && !(body instanceof URLSearchParams)) {
            headers['Content-Type'] = 'application/json';
            body = JSON.stringify(body);
        }

        if (method !== 'GET' && method !== 'HEAD') {
            const csrf = getCsrfToken();
            if (csrf) headers['X-XSRF-TOKEN'] = csrf;
        }

        const response = await fetch(NeilanConfig.api(url), {
            credentials: 'include',
            ...options,
            method,
            headers,
            body
        });

        if (response.status === 401 && !window.location.pathname.includes('login.html')) {
            window.location.href = 'login.html';
            throw new Error('Não autenticado');
        }

        return response;
    }

    async function parseJson(response) {
        if (response.status === 204) return null;
        const data = await response.json().catch(() => ({}));
        if (!response.ok) throw data;
        return data;
    }

    return {
        get: (url) => request(url).then(parseJson),

        post: (url, body) => request(url, { method: 'POST', body }).then(parseJson),

        put: (url, body) => request(url, { method: 'PUT', body }).then(parseJson),

        delete: (url) => request(url, { method: 'DELETE' }).then(r => {
            if (!r.ok && r.status !== 204) return parseJson(r);
            return null;
        }),

        login: async (email, senha) => {
            await ensureCsrf();
            const params = new URLSearchParams();
            params.append('email', email);
            params.append('senha', senha);
            const csrf = getCsrfToken();
            const res = await fetch(NeilanConfig.api('/api/auth/login'), {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/x-www-form-urlencoded',
                    'Accept': 'application/json',
                    ...(csrf ? { 'X-XSRF-TOKEN': csrf } : {})
                },
                body: params
            });
            return parseJson(res);
        },

        logout: () => request('/api/auth/logout', { method: 'POST' }).then(parseJson),

        me: () => fetch(NeilanConfig.api('/api/auth/me'), { credentials: 'include' })
            .then(r => r.ok ? r.json() : null),

        downloadCsv: async (inicio, fim) => {
            const params = new URLSearchParams();
            if (inicio) params.append('inicio', inicio);
            if (fim) params.append('fim', fim);
            const url = '/api/export/csv' + (params.toString() ? '?' + params : '');
            const res = await request(url);
            if (!res.ok) throw new Error('Erro ao exportar');
            return res.blob();
        }
    };
})();
