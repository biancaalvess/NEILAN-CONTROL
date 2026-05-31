# Neilan Control

Sistema monolítico de controle financeiro para **Neilan Estética Automotiva** — lavagens, estéticas e relatórios de lucro.

Stack: **Java 17**, **Spring Boot 3** (API REST), **HTML/CSS/JS** (frontend), **PostgreSQL**.

## Arquitetura

Monólito com **front e back separados**, conectados via API REST na mesma origem:

```
frontend/          → HTML, CSS, JavaScript (telas)
src/main/java/     → Spring Boot REST API (/api/**)
```

O Maven copia `frontend/` para `static/` no build. Um único JAR serve as páginas e a API.

| Camada | Pasta | Responsabilidade |
|--------|-------|------------------|
| **Front** | `frontend/` | Telas, estilos, chamadas `fetch` à API |
| **Back** | `src/main/java/` | Regras de negócio, banco, segurança |

## Login

- **E-mail:** `neilan@estetica.com`
- **Senha:** `neilan1958`

## Funcionalidades

- Registrar serviços, histórico, lucro diário/mensal/trimestral/anual
- Relatório visual com ranking, configuração de serviços e export CSV

## API REST

| Método | Rota | Descrição |
|--------|------|-----------|
| POST | `/api/auth/login` | Login (form-urlencoded) |
| POST | `/api/auth/logout` | Logout |
| GET | `/api/auth/me` | Usuário logado |
| GET | `/api/dashboard` | Resumos financeiros |
| GET/POST | `/api/servicos` | Listar / registrar |
| GET/POST/PUT/DELETE | `/api/tipos-servico` | Configuração |
| GET | `/api/relatorio?periodo=` | Relatório |
| GET | `/api/export/csv` | Download CSV |

## Executar localmente

```bash
.\mvnw.cmd spring-boot:run
```

Acesse: http://localhost:8080/login.html

## Deploy

**Railway:** conecte o repo, adicione PostgreSQL, `SPRING_PROFILES_ACTIVE=prod`.

**Vercel:** use `vercel.json` como proxy para o domínio Railway (Java não roda no Vercel).

---

Desenvolvido para Neilan Estética Automotiva — Heliópolis, BA
