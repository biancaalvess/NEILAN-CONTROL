document.addEventListener('DOMContentLoaded', async () => {
    const params = new URLSearchParams(window.location.search);

    if (params.get('error') === 'true') {
        NeilanUtils.showAlert('alert-box', 'E-mail ou senha incorretos.', 'error');
    }
    if (params.get('logout') === 'true') {
        NeilanUtils.showAlert('alert-box', 'Sessão encerrada com sucesso.', 'success');
    }

    const user = await NeilanApi.me();
    if (user) {
        window.location.href = 'index.html';
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
            window.location.href = 'index.html';
        } catch {
            NeilanUtils.showAlert('alert-box', 'E-mail ou senha incorretos.', 'error');
        } finally {
            btn.disabled = false;
        }
    });
});
