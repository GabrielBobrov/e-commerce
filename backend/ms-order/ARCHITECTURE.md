# Arquitetura do Projeto ms-order

Este documento descreve a arquitetura do microsserviço `ms-order`, que segue os princípios da **Arquitetura Hexagonal (Ports and Adapters)**.

## Visão Geral

O projeto está estruturado para isolar a lógica de domínio das dependências externas, como bancos de dados, frameworks web e serviços de mensageria. Isso facilita a testabilidade, manutenção e evolução do software.

## Estrutura de Pacotes

A estrutura de pacotes reflete as camadas da arquitetura hexagonal:

```
bobrov.order
├── core                # Núcleo da aplicação (Domínio e Casos de Uso)
│   ├── domain          # Entidades de domínio e Enums
│   ├── ports           # Interfaces que definem as entradas e saídas (Portas)
│   │   ├── in          # Portas de entrada (Casos de uso / Serviços)
│   │   └── out         # Portas de saída (Repositórios, Integrações externas)
│   ├── adapter         # Implementações da lógica de negócio (Serviços)
│   ├── mapper          # Mapeadores internos do Core
│   └── exception       # Exceções de domínio
├── entrypoint          # Adaptadores de Entrada (Driving Adapters)
│   ├── controller      # Controladores REST
│   ├── dto             # Data Transfer Objects para a API
│   ├── mapper          # Mapeadores DTO <-> Domínio
│   ├── exception       # Tratamento global de exceções (ControllerAdvice)
│   └── constants       # Constantes de API (URLs, etc.)
└── infrastructure      # Adaptadores de Saída (Driven Adapters)
    ├── persistence     # Implementações de persistência
    │   ├── entity      # Entidades JPA/Banco de dados
    │   ├── mapper      # Mapeadores Entidade <-> Domínio
    │   └── ...         # Repositórios JPA e Adaptadores
    ├── messaging       # Implementações de mensageria (Publishers e Listeners)
    │   ├── listener    # Listeners SQS
    │   └── ...         # Adaptadores SNS
    └── scheduler       # Jobs agendados (Outbox Pattern)
```

## Componentes Principais

### Core (Domínio)
O pacote `core` contém a lógica de negócios pura. Ele não depende de frameworks externos (como Spring Boot ou JPA) diretamente, exceto por anotações utilitárias ou padrões que não acoplam a infraestrutura.
- **Domain**: Classes que representam os conceitos do negócio (`Order`, `OrderEvent`, `OrderItem`).
- **Ports**: Interfaces que definem como o mundo externo interage com o domínio (`in`) e como o domínio interage com o mundo externo (`out`).
- **Adapter (Service)**: Implementações das portas de entrada (`in`). Aqui residem os serviços de domínio.

### Entrypoint (Entrada)
Responsável por receber as requisições externas e convertê-las para o formato que o domínio entende.
- **Controller**: Expõe endpoints HTTP.
- **DTOs**: Objetos usados para comunicação externa, desacoplando o modelo de domínio da API pública.

### Infrastructure (Infraestrutura)
Responsável por implementar as interfaces definidas nas portas de saída (`out`) do domínio.
- **Persistence**: Implementação de repositórios usando Spring Data JPA e Hibernate.
- **Messaging**: Implementações para comunicação assíncrona com AWS SNS e SQS.
- **Scheduler**: Implementação do padrão Transactional Outbox.

## Tecnologias Utilizadas

Baseado no `.xml`:
- **Kotlin 1.9** (Java 17)
- **Spring Boot 3.2.2** (Web, Data JPA, Validation)
- **Banco de Dados**: PostgreSQL 16 (com Flyway para migrações)
- **Mensageria/Cloud**: Spring Cloud AWS 3.1.0 (SNS, SQS)
- **Infraestrutura Local**: Docker, Docker Compose, LocalStack
- **IaC**: Terraform
- **Testes**: JUnit 5, Kotlin Test
- **Ferramentas**: Maven, Lombok (opcional)

## Fluxo de Dados Típico (Síncrono)

1. Uma requisição chega ao **Entrypoint** (ex: Controller REST).
2. O Controller converte o DTO para um Modelo de Domínio (usando Mappers).
3. O Controller invoca uma Porta de Entrada (`core.ports.in`).
4. A implementação da porta (`core.adapter.service`) executa a lógica de negócio.
5. Se necessário, o domínio invoca uma Porta de Saída (`core.ports.out`) para buscar ou persistir dados.
6. A **Infrastructure** implementa essa porta de saída (ex: Repositório JPA) e acessa o banco de dados.
7. O resultado retorna através das camadas até ser enviado de volta ao cliente pelo Controller.

## Fluxo Assíncrono (Transactional Outbox)

1. **Service**: Recebe a solicitação e salva um evento (`OrderEvent`) no banco de dados.
2. **Scheduler**: Lê eventos pendentes e publica no SNS.
3. **Listener**: Consome a mensagem do SQS (via SNS), processa a lógica de negócio e salva o pedido final no banco.
