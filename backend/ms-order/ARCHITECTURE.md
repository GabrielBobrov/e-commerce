# Arquitetura do Projeto ms-account

Este documento descreve a arquitetura do microsserviço `ms-account`, que segue os princípios da **Arquitetura Hexagonal (Ports and Adapters)**.

## Visão Geral

O projeto está estruturado para isolar a lógica de domínio das dependências externas, como bancos de dados, frameworks web e serviços de mensageria. Isso facilita a testabilidade, manutenção e evolução do software.

## Estrutura de Pacotes

A estrutura de pacotes reflete as camadas da arquitetura hexagonal:

```
com.ms.account
├── core                # Núcleo da aplicação (Domínio e Casos de Uso)
│   ├── model           # Entidades de domínio
│   ├── ports           # Interfaces que definem as entradas e saídas (Portas)
│   │   ├── in          # Portas de entrada (Casos de uso / Serviços)
│   │   └── out         # Portas de saída (Repositórios, Integrações externas)
│   ├── adapter         # Implementações da lógica de negócio (Casos de uso)
│   ├── mapper          # Mapeadores entre objetos de domínio e outros objetos
│   └── exception       # Exceções de domínio
├── entrypoint          # Adaptadores de Entrada (Driving Adapters)
│   ├── rest            # Controladores REST
│   ├── dto             # Data Transfer Objects para a API
│   ├── mapper          # Mapeadores DTO <-> Modelo
│   ├── controller      # Controladores (pode ser redundante com 'rest', verificar uso)
│   └── ...             # Configurações de API, Swagger, etc.
└── infrastructure      # Adaptadores de Saída (Driven Adapters)
    ├── data            # Implementações de persistência
    ├── entity          # Entidades JPA/Banco de dados
    ├── repository      # Interfaces de repositório (Spring Data)
    ├── aws             # Integrações com AWS (SQS, etc.)
    └── ...             # Outras configurações de infraestrutura
```

## Componentes Principais

### Core (Domínio)
O pacote `core` contém a lógica de negócios pura. Ele não depende de frameworks externos (como Spring Boot ou JPA) diretamente, exceto por anotações utilitárias (como Lombok) ou padrões que não acoplam a infraestrutura.
- **Model**: Classes que representam os conceitos do negócio.
- **Ports**: Interfaces que definem como o mundo externo interage com o domínio (`in`) e como o domínio interage com o mundo externo (`out`).
- **Adapter (Core)**: Implementações das portas de entrada (`in`). Aqui residem os serviços de domínio.

### Entrypoint (Entrada)
Responsável por receber as requisições externas e convertê-las para o formato que o domínio entende.
- **REST/Controller**: Expõe endpoints HTTP.
- **DTOs**: Objetos usados para comunicação externa, desacoplando o modelo de domínio da API pública.

### Infrastructure (Infraestrutura)
Responsável por implementar as interfaces definidas nas portas de saída (`out`) do domínio.
- **Persistence**: Implementação de repositórios usando Spring Data JPA, Hibernate, etc.
- **Messaging/AWS**: Implementações para comunicação assíncrona (Kafka, SQS).

## Tecnologias Utilizadas

Baseado no `pom.xml`:
- **Java 17**
- **Spring Boot 3.1.5** (Web, Data JPA, Validation, Hateoas, Webflux)
- **Banco de Dados**: MySQL (com Flyway para migrações)
- **Mapeamento**: MapStruct
- **Documentação**: SpringDoc OpenAPI (Swagger)
- **Mensageria/Cloud**: Spring Cloud AWS (SQS), Spring Kafka
- **Testes**: JUnit, JavaFaker
- **Ferramentas**: Lombok, Docker

## Fluxo de Dados Típico

1. Uma requisição chega ao **Entrypoint** (ex: Controller REST).
2. O Controller converte o DTO para um Modelo de Domínio (usando Mappers).
3. O Controller invoca uma Porta de Entrada (`core.ports.in`).
4. A implementação da porta (`core.adapter`) executa a lógica de negócio.
5. Se necessário, o domínio invoca uma Porta de Saída (`core.ports.out`) para buscar ou persistir dados.
6. A **Infrastructure** implementa essa porta de saída (ex: Repositório JPA) e acessa o banco de dados.
7. O resultado retorna através das camadas até ser enviado de volta ao cliente pelo Controller.
