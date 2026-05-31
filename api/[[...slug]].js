export const config = {
    runtime: 'edge'
};

const BACKEND = process.env.RAILWAY_BACKEND_URL
    || 'https://neilan-control-production.up.railway.app';

export default async function handler(request) {
    const url = new URL(request.url);
    const targetUrl = BACKEND + url.pathname + url.search;

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

    return fetch(targetUrl, init);
}
