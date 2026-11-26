# Etapa 1: Compilación de la aplicación con JDK 17
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copiar archivos de Gradle para aprovechar caché de capas
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

# Copiar código fuente y empaquetar jar sin ejecutar tests (ya testeado)
COPY src src
RUN ./gradlew bootJar -x test --no-daemon

# Etapa 2: Imagen liviana de ejecución con JRE 17
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copiar el jar generado desde la etapa anterior
COPY --from=builder /app/build/libs/backend-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
