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

Com esta abordagem, a aplicação e toda a sua infraestrutura (Banco de Dados, Cache, Mensageria) rodam em contêineres Docker.

### Pré-requisitos

*   Docker e Docker Compose
*   Terraform (para criar os recursos da "AWS" local)
*   AWS CLI (opcional, para interagir com o LocalStack)

### Passo 1: Subir todo o ambiente com Docker Compose

Na raiz do projeto, execute o comando:

```bash
docker-compose up --build
```

Este único comando irá:
1.  Construir a imagem Docker da sua API a partir do `Dockerfile`.
2.  Iniciar os contêineres da **API**, **PostgreSQL**, **Redis** e **LocalStack**.
3.  Conectar todos os serviços em uma rede interna.

Deixe este terminal rodando para acompanhar os logs de todos os serviços.

### Passo 2: Criar os Recursos da AWS com Terraform

O LocalStack sobe "vazio". Use o Terraform para criar o tópico SNS e a fila SQS. **Abra um novo terminal** e execute:

```bash
# 1. Navegue até a pasta do Terraform
cd terraform

# 2. Inicialize o Terraform (apenas na primeira vez)
terraform init

# 3. Crie os recursos no LocalStack
terraform apply -auto-approve
```

Após alguns segundos, você verá nos logs do `docker-compose` que a API conseguiu publicar os eventos com sucesso. A aplicação estará disponível em `http://localhost:8080`.

---

## 📖 Como Rodar para Desenvolvimento (Alternativa)

Se você precisa fazer debug da aplicação diretamente pela sua IDE, pode rodar a API localmente e usar o Docker Compose apenas para a infraestrutura.

1.  **Comente o serviço `api`** no arquivo `docker-compose.yml`.
2.  Suba a infraestrutura: `docker-compose up -d`.
3.  Crie os recursos com o Terraform (mesmo passo anterior).
4.  Rode a aplicação pela sua IDE ou com o comando `./mvnw spring-boot:run`.