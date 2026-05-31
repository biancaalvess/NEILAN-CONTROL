document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);

    if (params.get('error') === 'true') {
        NeilanUtils.showAlert('alert-box', 'E-mail ou senha incorretos.', 'error');
    }
    if (params.get('logout') === 'true') {
        NeilanUtils.showAlert('alert-box', 'Sessão encerrada com sucesso.', 'success');
    }

    const user = NeilanConfig.apiBase() ? null : await NeilanApi.me();
    if (user) {
        NeilanTransitions.navigate('index.html');
        return;
    }

    document.getElementById('login-form')?.addEventListener('submit', async (e) => {
        e.preventDefault();
        const email = document.getElementById('email').value.trim();
        const senha = document.getElementById('senha').value;
        const btn = document.getElementById('login-btn');
        btn.disabled = true;

        try {
            await NeilanApi.login(email, senha);
            NeilanTransitions.navigate('index.html');
        } catch (err) {
            const msg = err?.error || 'E-mail ou senha incorretos.';
            NeilanUtils.showAlert('alert-box', msg, 'error');
        } finally {
            btn.disabled = false;
        }
    });
});
