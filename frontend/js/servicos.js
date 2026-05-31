document.addEventListener('DOMContentLoaded', async () => {
    await initLayout('servicos.html');

    const inicioInput = document.getElementById('inicio');
    const fimInput = document.getElementById('fim');
    inicioInput.value = NeilanUtils.monthStartInput();
    fimInput.value = NeilanUtils.todayInput();

    document.getElementById('filtro-form')?.addEventListener('submit', (e) => {
        e.preventDefault();
        carregar();
    });

    document.getElementById('export-btn')?.addEventListener('click', async () => {
        try {
            const blob = await NeilanApi.downloadCsv(inicioInput.value, fimInput.value);
            const a = document.createElement('a');
            a.href = URL.createObjectURL(blob);
            a.download = `neilan-${inicioInput.value}-${fimInput.value}.csv`;
            a.click();
        } catch {
            alert('Erro ao exportar');
        }
    });

    await carregar();
});

async function carregar() {
    const inicio = document.getElementById('inicio').value;
    const fim = document.getElementById('fim').value;

    try {
        const data = await NeilanApi.get(`/api/servicos?inicio=${inicio}&fim=${fim}`);
        document.getElementById('summary-bar').innerHTML = `
            <span><strong>${data.quantidade}</strong> serviço(s) no período</span>
            <span>Total: <strong>${NeilanUtils.formatMoney(data.totalPeriodo)}</strong></span>`;

        const container = document.getElementById('servicos-container');
        if (!data.servicos.length) {
            container.innerHTML = `<div class="empty-state"><div class="icon">📋</div><p>Nenhum serviço encontrado neste período.</p></div>`;
            return;
        }

        container.innerHTML = `
            <div class="table-container">
                <table>
                    <thead><tr><th>Data/Hora</th><th>Serviço</th><th>Categoria</th><th>Cliente</th><th>Placa</th><th>Valor</th><th>Obs.</th></tr></thead>
                    <tbody>${data.servicos.map(s => `
                        <tr>
                            <td>${NeilanUtils.formatDateTime(s.dataHora)}</td>
                            <td>${NeilanUtils.escapeHtml(s.tipoServicoNome)}</td>
                            <td><span class="badge badge-categoria">${NeilanUtils.escapeHtml(s.categoria)}</span></td>
                            <td>${NeilanUtils.escapeHtml(s.clienteNome || '-')}</td>
                            <td>${NeilanUtils.escapeHtml(s.placa || '-')}</td>
                            <td class="valor">${NeilanUtils.formatMoney(s.valor)}</td>
                            <td>${NeilanUtils.escapeHtml(s.observacoes || '-')}</td>
                        </tr>
                    `).join('')}</tbody>
                </table>
            </div>`;
    } catch { /* */ }
}
