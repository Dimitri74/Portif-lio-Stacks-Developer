# Java MongoDB — Formulário com ViaCEP

API REST em Spring Boot para cadastro de formulário com endereço preenchido automaticamente via ViaCEP e persistência no MongoDB Atlas.

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
viacep.base-url=https://viacep.com.br/ws
server.port=8080
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
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
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
