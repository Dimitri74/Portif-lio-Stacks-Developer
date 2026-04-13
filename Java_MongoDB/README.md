# Java MongoDB — Formulário com ViaCEP

API REST em Spring Boot para cadastro de formulário com endereço preenchido automaticamente via ViaCEP e persistência no MongoDB Atlas.

---

## Tecnologias utilizadas

| Ícone | Tecnologia | O que é / o que faz no projeto |
|---|---|---|
| ![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white) | **Java 21** | Linguagem principal da aplicação. É a base de toda a API, regras de negócio, integrações e testes. |
| ![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.5.13-6DB33F?logo=springboot&logoColor=white) | **Spring Boot** | Framework principal do backend. Facilita a configuração da aplicação, inicialização do projeto e exposição da API REST. |
| ![Spring Web](https://img.shields.io/badge/Spring_Web-REST-6DB33F?logo=spring&logoColor=white) | **Spring Web** | Módulo usado para criar os endpoints HTTP da aplicação, receber requisições JSON e devolver respostas da API. |
| ![Spring Data MongoDB](https://img.shields.io/badge/Spring_Data_MongoDB-persist%C3%AAncia-6DB33F?logo=spring&logoColor=white) | **Spring Data MongoDB** | Camada de persistência que simplifica o acesso ao MongoDB usando repositórios e mapeamento de documentos Java. |
| ![MongoDB](https://img.shields.io/badge/MongoDB_Atlas-cloud-47A248?logo=mongodb&logoColor=white) | **MongoDB Atlas** | Banco de dados NoSQL em nuvem onde os formulários são armazenados automaticamente em collections. |
| ![Maven](https://img.shields.io/badge/Maven-build-C71A36?logo=apachemaven&logoColor=white) | **Maven** | Ferramenta de build e gerenciamento de dependências. Compila, testa e executa o projeto. |
| ![Lombok](https://img.shields.io/badge/Lombok-produtividade-BC4521?logo=java&logoColor=white) | **Lombok** | Biblioteca que reduz código repetitivo, gerando automaticamente getters, setters, builders e construtores. |
| ![JUnit 5](https://img.shields.io/badge/JUnit_5-testes-25A162?logo=junit5&logoColor=white) | **JUnit 5** | Framework de testes usado para validar o comportamento da aplicação e evitar regressões. |
| ![Mockito](https://img.shields.io/badge/Mockito-mocks-78A641?logo=mockito&logoColor=white) | **Mockito** | Biblioteca usada nos testes para simular dependências como repositórios e clientes externos. |
| ![Postman](https://img.shields.io/badge/Postman-API_Test-F76935?logo=postman&logoColor=white) | **Postman** | Ferramenta para testar manualmente os endpoints da API. O projeto possui uma collection pronta para importação. |
| ![ViaCEP](https://img.shields.io/badge/ViaCEP-integra%C3%A7%C3%A3o_JSON-0A66C2?logo=json&logoColor=white) | **ViaCEP** | Webservice externo consultado pela aplicação para preencher automaticamente os dados de endereço a partir do CEP. |

---

## Pré-requisitos

| Ferramenta | Versão mínima |
|---|---|
| Java | 21 |
| Maven (ou use o `mvnw` incluso) | 3.8+ |
| Conta no MongoDB Atlas | — |

---

## Configuração antes de subir

### 1. Obter a URI do MongoDB Atlas

1. Acesse [https://cloud.mongodb.com](https://cloud.mongodb.com)
2. Clique em **Connect** no seu cluster `JavaMongoCluster`
3. Escolha **Drivers → Java**
4. Copie a URI no formato:
   ```
   mongodb+srv://marcusdimitri7_db_user:<password>@javamongocluster.1ewn9wg.mongodb.net/Java_MongoDB?retryWrites=true&w=majority&appName=JavaMongoCluster
   ```

### 2. Inserir a senha no application.properties

Abra o arquivo `src/main/resources/application.properties` e substitua `SUA_SENHA` pela senha real do seu usuário do Atlas:

```ini
spring.data.mongodb.uri=mongodb+srv://marcusdimitri7_db_user:SUA_SENHA@javamongocluster.1ewn9wg.mongodb.net/Java_MongoDB?retryWrites=true&w=majority&appName=JavaMongoCluster
spring.data.mongodb.database=Java_MongoDB
spring.data.mongodb.auto-index-creation=true
viacep.base-url=https://viacep.com.br/ws
server.port=8080
server.error.include-stacktrace=never
```

### 3. Liberar o IP no Atlas

1. No Atlas, acesse **Security → Network Access**
2. Clique em **+ Add IP Address**
3. Para desenvolvimento: clique em **Allow Access from Anywhere** (`0.0.0.0/0`)
4. Clique em **Confirm**

### 4. Confirmar o usuário do banco

1. No Atlas, acesse **Security → Database Access**
2. Confirme que o usuário `marcusdimitri7_db_user` tem a role **readWrite** no banco `Java_MongoDB`

---

## Rodando a aplicação

### Compilar e rodar

```powershell
cd "C:\Users\marcu\workspace\UNIPDS\Estudo-Stacks\Java_MongoDB"
.\mvnw.cmd spring-boot:run
```

### Confirmar que está no ar

No log do terminal, procure:

```
Finished Spring Data repository scanning ... Found 1 MongoDB repository interface.
Tomcat started on port 8080
Started JavaMongoDbApplication
```

---

## Endpoints disponíveis

### Base URL
```
http://localhost:8080/api/formularios
```

---

### Buscar endereço por CEP (sem salvar)

```http
GET /api/formularios/cep/{cep}
```

**Exemplo:**
```
GET http://localhost:8080/api/formularios/cep/01001000
```

**Resposta:**
```json
{
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "complemento": "lado ímpar",
  "unidade": "",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP",
  "estado": "São Paulo",
  "regiao": "Sudeste",
  "ibge": "3550308",
  "gia": "1004",
  "ddd": "11",
  "siafi": "7107"
}
```

---

### Criar formulário (salva no MongoDB)

```http
POST /api/formularios
Content-Type: application/json
```

**Body mínimo — o backend preenche o endereço via ViaCEP automaticamente:**
```json
{
  "nome": "Marcus",
  "email": "marcus@email.com",
  "cep": "01001000"
}
```

**Body completo com complemento customizado:**
```json
{
  "nome": "Marcus",
  "email": "marcus@email.com",
  "cep": "01001000",
  "complemento": "Apto 42"
}
```

### Regra de e-mail único

- Cada e-mail pode possuir apenas **um formulário** cadastrado
- Ao tentar criar ou atualizar um formulário com um e-mail já existente, a API retorna **409 Conflict**

**Exemplo de resposta:**
```json
{
  "timestamp": "2026-04-13T00:00:00Z",
  "status": 409,
  "error": "Conflict",
  "trace": "Email ja cadastrado"
}
```

---

### Listar todos os formulários

```http
GET http://localhost:8080/api/formularios
```

---

### Buscar formulário por ID

```http
GET http://localhost:8080/api/formularios/{id}
```

---

### Atualizar formulário

```http
PUT http://localhost:8080/api/formularios/{id}
Content-Type: application/json
```

```json
{
  "nome": "Marcus Dimitri",
  "email": "marcus@email.com",
  "cep": "04538133"
}
```

---

### Deletar formulário

```http
DELETE http://localhost:8080/api/formularios/{id}
```

---

## Como a collection é criada no MongoDB

O MongoDB **não exige criação manual** de tabelas/collections.

Ao salvar o primeiro formulário via `POST /api/formularios`:
- O banco `Java_MongoDB` é criado automaticamente no Atlas (se não existir)
- A collection `formularios` é criada automaticamente (se não existir)
- O documento é salvo com `_id` gerado

**Exemplo de documento salvo no Atlas:**
```json
{
  "_id": "6617f0abc123...",
  "nome": "Marcus",
  "email": "marcus@email.com",
  "cep": "01001-000",
  "logradouro": "Praça da Sé",
  "complemento": "Apto 42",
  "bairro": "Sé",
  "localidade": "São Paulo",
  "uf": "SP",
  "estado": "São Paulo",
  "regiao": "Sudeste",
  "ibge": "3550308",
  "gia": "1004",
  "ddd": "11",
  "siafi": "7107"
}
```

---

## Rodando os testes

```powershell
cd "C:\Users\marcu\workspace\UNIPDS\Estudo-Stacks\Java_MongoDB"
.\mvnw.cmd test -ntp
```

Resultado esperado:
```
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

> Os testes rodam sem precisar de conexão com o MongoDB.

---

## Estrutura do projeto

```
src/main/java/com/example/formulario/Java_MongoDB/
├── JavaMongoDbApplication.java
├── client/
│   └── ViaCepClient.java           ← consulta o ViaCEP
├── controller/
│   └── FormularioController.java   ← endpoints REST
├── dto/
│   ├── FormularioDTO.java          ← contrato HTTP
│   └── ViaCepResponseDTO.java      ← resposta do ViaCEP
├── model/
│   └── Formulario.java             ← documento MongoDB
├── repository/
│   └── FormularioRepository.java   ← acesso ao banco
└── service/
    └── FormularioService.java      ← regras de negócio
```

---

## Validações de CEP

| CEP enviado | Resultado |
|---|---|
| `01001000` (8 dígitos) | ✅ Consulta o ViaCEP |
| `01001-000` (com máscara) | ✅ Normalizado e consultado |
| `950100100` (9 dígitos) | ❌ 400 Bad Request |
| `95010A10` (alfanumérico) | ❌ 400 Bad Request |
| `99999999` (inexistente) | ❌ 404 Not Found |

---

## Padrão de resposta de erro

As respostas de erro da API retornam um JSON amigável com os campos:

- `timestamp`
- `status`
- `error`
- `trace` ← mensagem amigável de negócio ou validação, sem stacktrace técnico

**Exemplo de erro de CEP inválido:**
```json
{
  "timestamp": "2026-04-13T00:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "trace": "CEP invalido. Informe 8 digitos."
}
```

