const NeilanApi = (() => {
    let cachedCsrf = null;

    async function fetchCsrfToken() {
        const res = await fetch(NeilanConfig.api('/api/auth/csrf'), {
            credentials: 'include',
            headers: { Accept: 'application/json' }
        });
        if (res.status === 204 || res.status === 404) {
            return null;
        }
        if (!res.ok) {
            return null;
        }
        const data = await res.json();
        cachedCsrf = data.token;
        return cachedCsrf;
    }

    async function resolveCsrfToken() {
        if (cachedCsrf) {
            return cachedCsrf;
        }
        return fetchCsrfToken();
    }

    function clearCsrfCache() {
        cachedCsrf = null;
    }

    async function request(url, options = {}) {
        const method = (options.method || 'GET').toUpperCase();
        const headers = { ...(options.headers || {}) };
        let body = options.body;

        if (body && typeof body === 'object' && !(body instanceof FormData) && !(body instanceof URLSearchParams)) {
            headers['Content-Type'] = 'application/json';
            body = JSON.stringify(body);
        }

        if (method !== 'GET' && method !== 'HEAD') {
            const csrf = await resolveCsrfToken();
            if (csrf) {
                headers['X-XSRF-TOKEN'] = csrf;
            }
        }

        const response = await fetch(NeilanConfig.api(url), {
            credentials: 'include',
            ...options,
            method,
            headers,
            body
        });

        if (response.status === 401 && !window.location.pathname.includes('login.html')) {
            NeilanTransitions.navigate('login.html');
            throw new Error('Não autenticado');
        }

        return response;
    }

    async function parseJson(response) {
        if (response.status === 204) return null;
        const data = await response.json().catch(() => ({}));
        if (response.status === 403) {
            clearCsrfCache();
            throw {
                error: 'Falha de segurança (CSRF). Recarregue a página ou acesse http://localhost:8090/login.html'
            };
        }
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
            clearCsrfCache();
            const csrf = await resolveCsrfToken();
            const params = new URLSearchParams();
            params.append('email', email);
            params.append('senha', senha);
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
            const data = await parseJson(res);
            clearCsrfCache();
            return data;
        },

        logout: async () => {
            const result = await request('/api/auth/logout', { method: 'POST' }).then(parseJson);
            clearCsrfCache();
            return result;
        },

        me: () => fetch(NeilanConfig.api('/api/auth/me'), {
            credentials: 'include',
            headers: { Accept: 'application/json' }
        }).then(r => (r.ok ? r.json() : null)),

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
