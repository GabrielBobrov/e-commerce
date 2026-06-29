# --- Fase 1: O Construtor ---
# Usa uma imagem base com Maven e JDK 17 para compilar a aplicação
FROM maven:3.9-eclipse-temurin-17 AS builder

# Define o diretório de trabalho dentro do contêiner
WORKDIR /app

# Copia o pom.xml primeiro para aproveitar o cache de dependências do Docker
COPY pom.xml .

# Baixa todas as dependências do projeto
RUN mvn dependency:go-offline

# Copia o resto do código-fonte da aplicação
COPY src ./src

# Compila a aplicação e gera o JAR, pulando os testes
RUN mvn clean install -DskipTests


# --- Fase 2: O Executor ---
# Usa uma imagem base mínima, apenas com o Java Runtime Environment (JRE)
FROM eclipse-temurin:17-jre-alpine

# Define o diretório de trabalho
WORKDIR /app

# Copia o JAR gerado na fase anterior para a imagem final
COPY --from=builder /app/target/ms-order-0.0.1-SNAPSHOT.jar ./app.jar

# Expõe a porta em que a aplicação roda (para documentação e uso em redes)
EXPOSE 8080

# Define o comando para executar a aplicação quando o contêiner iniciar
ENTRYPOINT ["java", "-jar", "app.jar"]