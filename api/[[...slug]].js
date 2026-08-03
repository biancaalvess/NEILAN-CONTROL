export const config = {
    runtime: 'edge'
};

const BACKEND = (process.env.RAILWAY_BACKEND_URL || 'https://neilan-control-production.up.railway.app')
    .replace(/\/$/, '');

export default async function handler(request) {
    const url = new URL(request.url);
    const targetUrl = `${BACKEND}${url.pathname}${url.search}`;

    const headers = new Headers(request.headers);
    headers.delete('host');
    headers.set('x-forwarded-host', url.host);
    headers.set('x-forwarded-proto', url.protocol.replace(':', ''));

    const init = {
        method: request.method,
        headers,
        redirect: 'manual'
    };

    if (request.method !== 'GET' && request.method !== 'HEAD') {
        init.body = request.body;
    }

    try {
        const response = await fetch(targetUrl, init);
        return new Response(response.body, {
            status: response.status,
            statusText: response.statusText,
            headers: response.headers
        });
    } catch (error) {
        return new Response(JSON.stringify({
            error: 'Backend indisponível',
            detail: error.message,
            backend: BACKEND
        }), {
            status: 502,
            headers: { 'Content-Type': 'application/json' }
        });
    }
}
