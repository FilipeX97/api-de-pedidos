# =========================================================
# ETAPA 1 — Compilação da aplicação
# =========================================================

FROM maven:3.9.16-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .

RUN mvn -B -ntp dependency:go-offline

COPY src ./src

RUN mvn -B -ntp clean package -DskipTests


# =========================================================
# ETAPA 2 — Execução da aplicação
# =========================================================

FROM eclipse-temurin:21-jre-alpine

LABEL org.opencontainers.image.title="API de Pedidos" \
      org.opencontainers.image.description="API REST para gerenciamento de pedidos e pagamentos com Spring Boot" \
      org.opencontainers.image.source="https://github.com/FilipeX97/api-de-pedidos"

WORKDIR /app

RUN apk add --no-cache curl \
    && addgroup -S app \
    && adduser -S app -G app

COPY --from=build \
     --chown=app:app \
     /app/target/api-de-pedidos-*.jar \
     app.jar

USER app

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]