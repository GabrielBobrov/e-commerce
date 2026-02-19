# MS Order - Microsserviço de Pedidos

Este é um microsserviço de gerenciamento de pedidos desenvolvido em **Kotlin** com **Spring Boot**, seguindo os princípios de **Arquitetura Hexagonal (Ports and Adapters)** e **Domain-Driven Design (DDD)**.

O projeto implementa um fluxo de criação de pedidos assíncrono e resiliente utilizando o padrão **Transactional Outbox** com **AWS SNS** e **SQS** (simulados localmente via **LocalStack**).

## 🚀 Tecnologias Utilizadas

*   **Linguagem**: Kotlin 1.9 (Java 17)
*   **Framework**: Spring Boot 3.2.2
*   **Banco de Dados**: PostgreSQL 16
*   **Migração de Dados**: Flyway
*   **Mensageria**: AWS SNS (Simple Notification Service) e AWS SQS (Simple Queue Service)
*   **Infraestrutura Local**: Docker, Docker Compose e LocalStack
*   **IaC (Infraestrutura como Código)**: Terraform
*   **Documentação**: Swagger/OpenAPI (Planejado), Mermaid Diagrams

## 🏗️ Arquitetura

O projeto está organizado em camadas seguindo a Arquitetura Hexagonal:

*   **Core**: Contém o domínio, regras de negócio e portas (interfaces). Não depende de frameworks externos.
    *   `domain`: Entidades de domínio (`Order`, `OrderEvent`).
    *   `ports`: Interfaces de entrada (`in`) e saída (`out`).
    *   `service`: Implementação dos casos de uso.
*   **Entrypoint**: Adaptadores de entrada (ex: Controllers REST, Listeners).
    *   `controller`: Recebe requisições HTTP.
    *   `dto`: Objetos de transferência de dados externos.
*   **Infrastructure**: Adaptadores de saída (ex: Banco de Dados, Mensageria).
    *   `persistence`: Implementação JPA/Hibernate.
    *   `messaging`: Implementação de Publishers e Listeners AWS.
    *   `scheduler`: Jobs agendados (ex: Outbox Pattern).

## 🔄 Fluxo de Funcionamento (Transactional Outbox)

1.  **API**: Recebe o pedido via `POST /api/orders`.
2.  **Persistência Atômica**: O serviço salva o **Pedido** e um **Evento** (`published=false`) na mesma transação do banco de dados.
3.  **Scheduler (Batch)**: Um job roda a cada 20 segundos, busca eventos não publicados, envia para o SNS e marca como publicados.
4.  **Mensageria**: O SNS encaminha a mensagem para uma fila SQS.
5.  **Consumo**: Um Listener escuta a fila SQS e processa a mensagem (neste exemplo, simula o salvamento idempotente do pedido).

## 🛠️ Como Rodar Localmente

### Pré-requisitos

*   Docker e Docker Compose
*   Java 17+
*   Maven
*   Terraform (para criar recursos no LocalStack)
*   AWS CLI (opcional, para debug)

### Passo 1: Subir a Infraestrutura

Na raiz do projeto, execute:

```bash
docker-compose up -d
```

Isso subirá:
*   **PostgreSQL**: Porta `5433` (Usuário: `paymentuser`, Senha: `paymentpass`, Banco: `order_db`)
*   **LocalStack**: Porta `4566` (Simulando AWS)

### Passo 2: Criar Recursos AWS (Terraform)

O LocalStack inicia vazio. Use o Terraform para criar o Tópico SNS e a Fila SQS.

```bash
cd terraform
terraform init
terraform apply -auto-approve
```

### Passo 3: Rodar a Aplicação

Volte para a raiz e execute:

```bash
./mvnw spring-boot:run
```

A aplicação estará disponível em `http://localhost:8080`.

## 🧪 Testando a API

### Criar um Pedido (Async)

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "550e8400-e29b-41d4-a716-446655440000",
    "items": [
      {
        "productId": "a1b2c3d4-e5f6-7890-1234-567890abcdef",
        "productName": "Smartphone XYZ",
        "productSku": "SKU-12345",
        "quantity": 1,
        "unitPrice": 999.99
      }
    ],
    "shippingAddress": {
      "street": "Rua Teste",
      "city": "São Paulo",
      "state": "SP",
      "zipCode": "00000-000"
    }
  }'
```

### Verificar Logs

Acompanhe o console da aplicação para ver o fluxo:
1.  `[SERVICE]` Pedido salvo no banco.
2.  `[SCHEDULER]` Evento publicado no SNS (após ~20s).
3.  `[LISTENER]` Mensagem recebida do SQS e processada.

## 📚 Documentação Detalhada

Para mais detalhes sobre os endpoints e diagramas de sequência, consulte o arquivo [API_DOCS.md](API_DOCS.md).
