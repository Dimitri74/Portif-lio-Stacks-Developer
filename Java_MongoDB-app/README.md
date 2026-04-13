# Formulario Atlas Dashboard

Frontend moderno em Next.js 16, React 19 e Tailwind CSS 4 para operar o backend Spring Boot do projeto `Java_MongoDB`.

## Tecnologias utilizadas

| Icone | Tecnologia | Versao | O que e / o que faz no projeto |
|---|---|---|---|
| ![Next.js](https://img.shields.io/badge/Next.js-16.2.3-000000?logo=nextdotjs&logoColor=white) | Next.js | 16.2.3 | Framework React full-stack. Aqui entrega o App Router, renderizacao da UI e rotas API internas que fazem proxy para o backend Java. |
| ![React](https://img.shields.io/badge/React-19.2.4-149ECA?logo=react&logoColor=white) | React | 19.2.4 | Biblioteca para interfaces reativas. Controla estado, eventos e fluxo interativo do dashboard CRUD. |
| ![TypeScript](https://img.shields.io/badge/TypeScript-5.x-3178C6?logo=typescript&logoColor=white) | TypeScript | 5.x | Superset tipado de JavaScript. Garante contratos de dados (formularios, payloads e erros) com mais seguranca e manutencao. |
| ![Tailwind CSS](https://img.shields.io/badge/Tailwind_CSS-4.x-06B6D4?logo=tailwindcss&logoColor=white) | Tailwind CSS | 4.x | Framework utilitario de estilos. Monta layout, responsividade e tema visual moderno com classes consistentes. |
| ![Lucide](https://img.shields.io/badge/Lucide_React-1.8.0-334155?logo=lucide&logoColor=white) | Lucide React | 1.8.0 | Biblioteca de icones SVG. Usada para reforcar semantica visual de acoes, alertas e estatisticas no painel. |
| ![ESLint](https://img.shields.io/badge/ESLint-9.x-4B32C3?logo=eslint&logoColor=white) | ESLint | 9.x | Ferramenta de linting. Mantem padrao de codigo e ajuda a evitar erros comuns durante o desenvolvimento. |
| ![Node.js](https://img.shields.io/badge/Node.js-ambiente-5FA04E?logo=nodedotjs&logoColor=white) | Node.js + npm | ambiente de execucao | Runtime e gerenciador de pacotes usados para instalar dependencias e rodar scripts de build/dev/lint. |

## O que este app faz

- Lista todos os formularios persistidos no MongoDB
- Cria novos registros com enriquecimento de endereco por CEP
- Atualiza registros existentes
- Remove registros
- Consulta CEP antes do envio para mostrar preview do endereco
- Usa rotas internas do Next.js como proxy para evitar CORS no navegador

## Backend esperado

Por padrao, o frontend encaminha as chamadas para:

```bash
http://localhost:8080/api/formularios
```

Endpoints usados:

- `GET /api/formularios`
- `POST /api/formularios`
- `GET /api/formularios/{id}`
- `PUT /api/formularios/{id}`
- `DELETE /api/formularios/{id}`
- `GET /api/formularios/cep/{cep}`

## Configuracao

Crie um arquivo `.env.local` opcional se quiser apontar para outra URL de backend:

```bash
BACKEND_API_BASE_URL=http://localhost:8080/api/formularios
```

Existe um exemplo em `.env.example`.

## Como rodar

1. Inicie o backend Spring Boot na porta `8080`
2. Na pasta do frontend, rode:

```bash
npm install
npm run dev
```

3. Abra `http://localhost:3000`

## Validacao

Comandos validados neste projeto:

```bash
npm run build
npx eslint src/components/formulario-dashboard.tsx src/app src/lib
```

## Estrutura principal

- `src/app/page.tsx`: entrada da dashboard
- `src/components/formulario-dashboard.tsx`: UI, estado e fluxo CRUD
- `src/app/api/formularios/**`: proxy interno do Next.js para o backend Java
- `src/lib/backend-proxy.ts`: encaminhamento HTTP para a API Spring Boot
- `src/lib/types.ts`: tipos compartilhados do frontend
