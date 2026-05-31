document.addEventListener('DOMContentLoaded', function () {
    const menuToggle = document.querySelector('.menu-toggle');
    const sidebar = document.querySelector('.sidebar');

    if (menuToggle && sidebar) {
        menuToggle.addEventListener('click', function () {
            sidebar.classList.toggle('open');
        });
    }

    const servicoSelect = document.getElementById('tipoServicoId');
    const valorInput = document.getElementById('valor');

    if (servicoSelect && valorInput) {
        servicoSelect.addEventListener('change', function () {
            const option = servicoSelect.options[servicoSelect.selectedIndex];
            const preco = option.getAttribute('data-preco');
            if (preco) {
                valorInput.value = parseFloat(preco).toFixed(2);
            }
        });
    }

    const currentPath = window.location.pathname;
    document.querySelectorAll('.sidebar-nav a').forEach(function (link) {
        const href = link.getAttribute('href');
        if (href === currentPath || (href !== '/' && currentPath.startsWith(href))) {
            link.classList.add('active');
        } else if (href === '/' && currentPath === '/') {
            link.classList.add('active');
        }
    });
});
