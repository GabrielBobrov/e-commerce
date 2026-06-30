# MS Order - Microsserviço de Pedidos

Este é um microsserviço de gerenciamento de pedidos desenvolvido em **Kotlin** com **Spring Boot**, seguindo os princípios de **Arquitetura Hexagonal (Ports and Adapters)** e **Domain-Driven Design (DDD)**.

O projeto implementa um fluxo de criação de pedidos assíncrono e resiliente utilizando o padrão **Transactional Outbox** com **AWS SNS** e **SQS**. Utiliza **Redis** como camada de cache para otimização de consultas. O ambiente de desenvolvimento é totalmente containerizado com **Docker**.

## 🚀 Tecnologias Utilizadas

*   **Linguagem**: Kotlin 1.9 (Java 17)
*   **Framework**: Spring Boot 3.2.2
*   **Banco de Dados**: PostgreSQL 16
*   **Cache**: Redis 7
*   **Migração de Dados**: Flyway
*   **Mensageria**: AWS SNS (Simple Notification Service) e AWS SQS (Simple Queue Service)
*   **Infraestrutura Local**: Docker, Docker Compose e LocalStack
*   **IaC (Infraestrutura como Código)**: Terraform
*   **Documentação**: Swagger/OpenAPI (Planejado), Mermaid Diagrams

## 📚 Documentação do Projeto

Toda a documentação técnica, incluindo arquitetura, fluxos de dados, exemplos de API e diagramas, está centralizada nos seguintes arquivos:

*   **[ARCHITECTURE.md](ARCHITECTURE.md)**: Para uma visão detalhada da arquitetura do projeto, organização de pacotes e componentes.
*   **[API_DOCS.md](API_DOCS.md)**: Para documentação das funcionalidades, contratos de API, exemplos de requisições e diagramas de sequência.

## 🛠️ Como Rodar Localmente (Método Recomendado)

O ambiente é totalmente containerizado. O fluxo de inicialização é dividido em etapas para garantir que a infraestrutura esteja pronta antes da API iniciar.

### Pré-requisitos

*   Docker e Docker Compose
*   Terraform
*   AWS CLI (opcional, para interagir com o LocalStack)

### Passo 1: Subir a Infraestrutura Base

Primeiro, vamos iniciar apenas os serviços de infraestrutura (Banco de Dados, Cache e o simulador da AWS).

Na raiz do projeto, execute:
```bash
docker-compose up -d db redis localstack
```
O `-d` (detached) sobe os contêineres em segundo plano. Aguarde alguns segundos para que eles iniciem completamente.

### Passo 2: Criar os Recursos da AWS com Terraform

Com o LocalStack no ar, vamos criar os recursos de mensageria (SNS/SQS) dentro dele.

```bash
# 1. Navegue até a pasta do Terraform
cd terraform

# 2. Crie e selecione o workspace local (apenas na primeira vez)
terraform workspace new local

# 3. Inicialize o Terraform (apenas na primeira vez)
terraform init

# 4. Aplique a configuração para criar os recursos
terraform apply -var="db_password=paymentpass"
```
Ao final, digite `yes` para confirmar. Isso criará o tópico SNS e as filas SQS no LocalStack.

### Passo 3: Subir a Aplicação

Agora que toda a infraestrutura está pronta e configurada, podemos iniciar a API.

Na raiz do projeto, execute:
```bash
docker-compose up -d --build api
```
Este comando irá construir a imagem da API (se necessário) e iniciá-la. Como os outros serviços já estão rodando, a API se conectará a eles com sucesso.

A aplicação estará disponível em `http://localhost:8080`.

### Para Parar o Ambiente

Para parar todos os contêineres, execute:
```bash
docker-compose down
```

---

## 📖 Como Rodar para Desenvolvimento (Alternativa)

Se você precisa fazer debug da aplicação diretamente pela sua IDE:

1.  Execute o **Passo 1** e o **Passo 2** acima para ter a infraestrutura no ar.
2.  Rode a aplicação pela sua IDE ou com o comando `./mvnw spring-boot:run`. A API local se conectará aos serviços que estão rodando nos contêineres.