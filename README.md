# 📊 API de Tabela Tarifária de Água

Sistema REST completo para gerenciar e calcular tarifas de água com base em categorias de consumidores e faixas de consumo progressivas.

## 📋 Índice

- [Características](#características)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Execução](#execução)
- [API Endpoints](#api-endpoints)
- [Exemplos de Uso](#exemplos-de-uso)
- [Testes](#testes)
- [Estrutura do Projeto](#estrutura-do-projeto)
- [Tecnologias](#tecnologias)

---

## ✨ Características

✅ **Parametrização Completa** - Tabelas tarifárias gerenciáveis sem recompilar  
✅ **Cálculo Progressivo** - Tarifas em faixas progressivas de consumo  
✅ **4 Categorias** - INDUSTRIAL, COMERCIAL, PARTICULAR, PUBLICO  
✅ **Migrações Automáticas** - Flyway para versionamento de schema  
✅ **Validações** - Regras de negócio garantidas  
✅ **Ambiente Seguro** - Variáveis de ambiente para credenciais  
✅ **Documentação OpenAPI** - Swagger UI integrado  

---

## 📦 Pré-requisitos

### **Obrigatório**

| Ferramenta | Versão | Download |
|-----------|--------|----------|
| **Java** | 21+ | [oracle.com/java](https://www.oracle.com/java/technologies/downloads/#java21) |
| **Maven** | 3.8.0+ | [maven.apache.org](https://maven.apache.org/download.cgi) |
| **PostgreSQL** | 12+ | [postgresql.org](https://www.postgresql.org/download/) |
| **Git** | Qualquer | [git-scm.com](https://git-scm.com/download) |

### **Verificar Versões**

```bash
java -version
mvn -version
psql --version
git --version
```

---

## 🚀 Instalação

### **1. Clonar Repositório**

```bash
git clone https://github.com/Davidbjjj/TarifAgua.git
cd tabelaAgua
```

### **2. Instalar Dependências**

```bash
mvn clean install
```

### **3. Criar Banco de Dados**

```sql
-- Conectar como superuser
psql -U postgres

-- Criar banco
CREATE DATABASE tabela_agua;

```

---

## ⚙️ Configuração

### **1. Criar Arquivo `.env`**

Copie o template:

```bash
cp .env
```

### **2. Editar `.env`**

```ini
# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=tabela_agua
DB_USERNAME=tabela_user
DB_PASSWORD=senha

# Application
APP_NAME=tabelaAgua
APP_ENV=development

# JPA/Hibernate
JPA_SHOW_SQL=true
JPA_FORMAT_SQL=true
JPA_DDL_AUTO=validate

# Flyway
FLYWAY_ENABLED=true
FLYWAY_BASELINE_ON_MIGRATE=true

# Logging
LOG_LEVEL_HIBERNATE=DEBUG
LOG_LEVEL_FLYWAY=INFO
```

### **3. Executar Migrações SQL**

Execute a query SQL fornecida para criar as tabelas:

```sql
-- Executar queries de criação de tabelas e dados iniciais
-- Ver arquivo: SETUP_DATABASE.sql
```

---

## 🎯 Execução

### **Windows (PowerShell)**

```powershell
# Carregar variáveis de ambiente
$env:DB_HOST="localhost"
$env:DB_PORT="5432"
$env:DB_NAME="tabela_agua"
$env:DB_USERNAME="tabela_user"
$env:DB_PASSWORD="senha"

# Iniciar aplicação
./mvnw spring-boot:run
```

### **Linux/Mac (Bash)**

```bash
# Carregar variáveis de ambiente
export DB_HOST=localhost
export DB_PORT=5432
export DB_NAME=tabela_agua
export DB_USERNAME=tabela_user
export DB_PASSWORD=senha

# Iniciar aplicação
./mvnw spring-boot:run
```

### **Com Variáveis no Comando**

```bash
./mvnw spring-boot:run \
  -Dspring.datasource.url=jdbc:postgresql://localhost:5432/tabela_agua \
  -Dspring.datasource.username=tabela_user \
  -Dspring.datasource.password=senha
```

### **Acessar Aplicação**

- **API**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI Docs**: http://localhost:8080/v3/api-docs

---

## 📡 API Endpoints

### **1. Criar Tabela Tarifária**

**Endpoint:**
```
POST /api/tabelas-tarifarias
```

**Request:**
```json
{
  "nome": "Tabela Oficial 2024",
  "vigencia": "2024-01-01",
  "categorias": [
    {
      "categoria": "INDUSTRIAL",
      "faixas": [
        {
          "inicio": 0,
          "fim": 10,
          "valorUnitario": 1.00
        },
        {
          "inicio": 11,
          "fim": 20,
          "valorUnitario": 2.00
        },
        {
          "inicio": 21,
          "fim": 99999,
          "valorUnitario": 3.00
        }
      ]
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "id": 1,
  "nome": "Tabela Oficial 2024",
  "vigencia": "2024-01-01",
  "active": true,
  "createdAt": "2026-02-08T10:30:00",
  "updatedAt": "2026-02-08T10:30:00"
}
```

---

### **2. Listar Tabelas Tarifárias**

**Endpoint:**
```
GET /api/tabelas-tarifarias
```

**Response (200 OK):**
```json
[
  {
    "id": 1,
    "nome": "Tabela Oficial 2024",
    "vigencia": "2024-01-01",
    "active": true,
    "createdAt": "2026-02-08T10:30:00",
    "updatedAt": "2026-02-08T10:30:00"
  }
]
```

---

### **3. Deletar Tabela Tarifária**

**Endpoint:**
```
DELETE /api/tabelas-tarifarias/{id}
```

**Response (204 No Content):**
```
(sem corpo)
```

---

### **4. Calcular Tarifa**

**Endpoint:**
```
POST /api/calculos
```

**Request:**
```json
{
  "categoria": "INDUSTRIAL",
  "consumo": 18
}
```

**Response (200 OK):**
```json
{
  "categoria": "INDUSTRIAL",
  "consumoTotal": 18,
  "valorTotal": 26.00,
  "detalhamento": [
    {
      "faixa": {
        "inicio": 0,
        "fim": 10
      },
      "m3Cobrados": 10,
      "valorUnitario": 1.00,
      "subtotal": 10.00
    },
    {
      "faixa": {
        "inicio": 11,
        "fim": 20
      },
      "m3Cobrados": 8,
      "valorUnitario": 2.00,
      "subtotal": 16.00
    }
  ]
}
```

---


## 💻 Exemplos de Uso

### **cURL**

```bash
# 1. Criar tabela
curl -X POST http://localhost:8080/api/tabelas-tarifarias \
  -H "Content-Type: application/json" \
  -d '{
    "nome": "Tabela 2024",
    "vigencia": "2024-01-01",
    "categorias": [{
      "categoria": "INDUSTRIAL",
      "faixas": [
        {"inicio": 0, "fim": 10, "valorUnitario": 1.00},
        {"inicio": 11, "fim": 20, "valorUnitario": 2.00},
        {"inicio": 21, "fim": 99999, "valorUnitario": 3.00}
      ]
    }]
  }'

# 2. Calcular tarifa
curl -X POST http://localhost:8080/api/calculos \
  -H "Content-Type: application/json" \
  -d '{"categoria":"INDUSTRIAL","consumo":18}'

# 3. Listar faixas
curl http://localhost:8080/api/parametrizacao-test/faixas/INDUSTRIAL

# 4. Alterar valor
curl -X PUT http://localhost:8080/api/parametrizacao-test/faixas/alterar-por-range \
  -H "Content-Type: application/json" \
  -d '{
    "categoria": "INDUSTRIAL",
    "inicio": 11,
    "fim": 20,
    "novoValor": 3.50
  }'
```

### **Postman**

1. Importe a coleção: `Testes-Parametrizacao.postman_collection.json`
2. Configure variável `baseUrl` = `http://localhost:8080`
3. Execute os requests na ordem

### **HTTP Client (VS Code)**

Crie arquivo `requests.http`:

```http
### Criar tabela
POST http://localhost:8080/api/tabelas-tarifarias
Content-Type: application/json

{
  "nome": "Tabela 2024",
  "vigencia": "2024-01-01",
  "categorias": [{
    "categoria": "INDUSTRIAL",
    "faixas": [
      {"inicio": 0, "fim": 10, "valorUnitario": 1.00},
      {"inicio": 11, "fim": 20, "valorUnitario": 2.00},
      {"inicio": 21, "fim": 99999, "valorUnitario": 3.00}
    ]
  }]
}

### Calcular
POST http://localhost:8080/api/calculos
Content-Type: application/json

{
  "categoria": "INDUSTRIAL",
  "consumo": 18
}
```

---

## 🧪 Testes

### **Executar Testes Unitários**

```bash
mvn test
```

### **Executar Testes de Integração**

```bash
mvn verify
```

### **Testes com Cobertura**

```bash
mvn clean test jacoco:report
# Acessar: target/site/jacoco/index.html
```

### **Validações de Regra de Negócio**

1. ✅ **Não Sobreposição**: Faixas não se cruzam
2. ✅ **Ordem Válida**: início < fim
3. ✅ **Cobertura Completa**: Começa em 0
4. ✅ **Cobertura Suficiente**: Vai até 99999
5. ✅ **Cálculo Progressivo**: Por faixas
6. ✅ **Parametrização**: Mudanças refletem sem reiniciar

---

## 📁 Estrutura do Projeto

```
tabelaAgua/
├── src/
│   ├── main/
│   │   ├── java/com/tarifaria/tabelaAgua/
│   │   │   ├── config/               # Configurações (OpenAPI)
│   │   │   ├── controller/           # Controllers REST
│   │   │   ├── dto/                  # Data Transfer Objects
│   │   │   ├── exception/            # Tratamento de exceções
│   │   │   ├── model/                # Entidades JPA
│   │   │   ├── repository/           # Repositories
│   │   │   ├── service/              # Serviços (lógica)
│   │   │   └── swagger/              # Documentação
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/         # Scripts Flyway
│   └── test/
│       └── java/                     # Testes
├── .env                              # Variáveis de ambiente
├── .env.example                      # Template
├── pom.xml                           # Dependências Maven
└── README.md                         # Este arquivo
```

---

## 🛠️ Tecnologias

| Tecnologia | Versão | Função |
|-----------|--------|--------|
| **Spring Boot** | 3.2.2 | Framework principal |
| **Spring Data JPA** | 3.2.2 | ORM/Persistência |
| **PostgreSQL** | 12+ | Banco de dados |
| **Flyway** | 9.22.3 | Migrações |
| **Lombok** | 1.18.30 | Reduzir boilerplate |
| **SpringDoc OpenAPI** | 2.1.0 | Swagger/OpenAPI |
| **JUnit 5** | 5.10.1 | Testes |
| **Mockito** | 5.7.0 | Mocks para testes |

