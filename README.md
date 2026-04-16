# SplitBill API 💸 - Em desenvolvimento!

SplitBill API é uma API REST para gerenciamento de despesas compartilhadas em grupo com cálculo automático de saldo entre participantes.

Projeto desenvolvido com **Java + Spring Boot**, seguindo boas práticas de arquitetura backend e organização colaborativa via GitHub.

---

## 📌 Objetivo do projeto

Este projeto tem como objetivo permitir que usuários criem grupos, registrem despesas compartilhadas e visualizem automaticamente quem deve pagar quem.

Exemplo de uso:

* Grupo de viagem
* Moradores dividindo contas
* Amigos dividindo restaurante
* Projetos universitários com custos compartilhados

A API calcula os saldos automaticamente entre os participantes.

---

## 🛠️ Tecnologias utilizadas

* Java
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* JWT (autenticação)
* MySQL
* Lombok
* Swagger / OpenAPI
* Maven

---

## 🧱 Arquitetura do projeto

O projeto segue arquitetura em camadas:

```
controller → recebe requisições HTTP
service → regras de negócio
repository → acesso ao banco
entity → entidades JPA
DTO → comunicação entre camadas
security → autenticação JWT
config → configurações globais
exception → tratamento de erros
```

---

## 📂 Estrutura inicial do projeto

```
src
 └── main
     └── java
         └── com.project.expenses
             ├── controller
             ├── service
             ├── repository
             ├── entity
             ├── dto
             ├── security
             ├── config
             └── exception
```

---

## 🚀 Funcionalidades planejadas

### Usuários

* Cadastro de usuário
* Login com JWT
* Autenticação segura

### Grupos

* Criar grupo
* Adicionar participantes
* Listar grupos do usuário

### Despesas

* Registrar despesa
* Definir quem pagou
* Definir participantes da divisão

### Cálculo automático

* Cálculo de saldo entre usuários
* Quem deve pagar quem
* Resumo financeiro por grupo

---

## 🔐 Autenticação

A API utiliza autenticação **JWT (JSON Web Token)**.

Endpoints públicos:

```
POST /auth/register
POST /auth/login
```

Endpoints protegidos exigem token JWT.

---

## 🗄️ Banco de dados

Banco utilizado:

```
MySQL
```

Configuração padrão esperada:

```
database: expense_manager_db
```

---

## ▶️ Como executar o projeto

### 1️⃣ Clonar repositório

```
git clone <repo-url>
```

### 2️⃣ Configurar banco MySQL

Criar banco:

```
expense_manager_db
```

Editar:

```
application.properties
```

### 3️⃣ Rodar aplicação

```
./mvnw spring-boot:run
```

Ou via IDE.

---

## 📖 Documentação da API

Após iniciar o projeto:

```
http://localhost:8080/swagger-ui.html
```

---

## 👥 Equipe

Projeto desenvolvido colaborativamente como prática profissional de backend.

Responsabilidades divididas entre:

* arquitetura
* autenticação
* modelagem de dados
* regras de negócio
* endpoints REST

---

## 📈 Status do projeto

🚧 Em desenvolvimento

Funcionalidades sendo implementadas por etapas (sprints).

---

## ⭐ Objetivo acadêmico e profissional

Este projeto foi criado para:

* prática de Spring Boot
* experiência com arquitetura real
* trabalho em equipe com GitHub
* simulação de ambiente profissional
* fortalecimento de portfólio backend

---

## 📄 Licença

Uso educacional.
