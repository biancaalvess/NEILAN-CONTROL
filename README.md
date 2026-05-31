# Neilan Control

Sistema monolítico de controle financeiro para **Neilan Estética Automotiva** — lavagens, estéticas e relatórios de lucro.

Stack: **Java 17**, **Spring Boot 3**, **Thymeleaf**, **HTML/CSS**, **PostgreSQL**.

## Funcionalidades

- **Registrar serviços** — lavagens e estéticas com cliente, placa e valor
- **Visualizar serviços feitos** — histórico com filtro por período
- **Lucro diário, mensal, trimestral e anual** — dashboard e relatórios
- **Relatório visual** — ranking por tipo de serviço com barras
- **Configuração** — cadastrar serviços, preços e ativar/desativar
- **Exportar CSV** — download dos dados filtrados

Serviços pré-cadastrados conforme [neilan-estetica.vercel.app](https://neilan-estetica.vercel.app/):

| Serviço | Categoria |
|---------|-----------|
| Polimento | Estética Automotiva |
| Cristalização e Vitrificação | Estética Automotiva |
| Lavagem de Bancos e Estofados | Lavagem de Estofados |
| Higienização de Teto | Lavagem de Estofados |
| Lavagem Completa | Lavagens em Geral |
| Lavagem de Motor | Lavagens em Geral |

## Executar localmente

Requisitos: Java 17+ e Maven.

```bash
mvn spring-boot:run
```

Acesse: http://localhost:8080

O perfil `local` usa banco H2 em memória (dados resetam ao reiniciar).

## Deploy no Railway (recomendado)

1. Crie um projeto no [Railway](https://railway.app)
2. Adicione um serviço **PostgreSQL**
3. Adicione um serviço conectado ao repositório Git (ou deploy via Dockerfile)
4. Configure a variável de ambiente:

```
SPRING_PROFILES_ACTIVE=prod
```

O Railway injeta `DATABASE_URL` automaticamente ao vincular o PostgreSQL.

5. Gere um domínio público em **Settings → Networking**

## Deploy no Vercel (proxy)

O Vercel não executa Java nativamente. Use-o como **proxy** para o app no Railway:

1. Edite `vercel.json` e substitua `SEU-APP.up.railway.app` pelo domínio real do Railway
2. Conecte o repositório no [Vercel](https://vercel.com)
3. Deploy — todas as rotas serão redirecionadas ao Spring Boot no Railway

Alternativa: use apenas o Railway com domínio próprio.

## Estrutura

```
src/main/java/com/neilan/control/
├── controller/     # Rotas web
├── service/        # Lógica financeira
├── model/          # Entidades JPA
├── repository/     # Acesso ao banco
└── config/         # Seed, banco, datas

src/main/resources/
├── templates/      # Páginas HTML (Thymeleaf)
├── static/css/     # Estilo (tema escuro + dourado)
└── application*.properties
```

## Rotas

| Rota | Descrição |
|------|-----------|
| `/` | Dashboard com lucros |
| `/registrar` | Registrar serviço |
| `/servicos` | Listar serviços |
| `/relatorio` | Relatório visual |
| `/configuracao` | Configurar serviços |
| `/export/csv` | Download CSV |

---

Desenvolvido para Neilan Estética Automotiva — Heliópolis, BA
