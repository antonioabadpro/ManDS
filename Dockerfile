# -------------------------------------------------------------
# FASE 1: Compilación de la aplicación
# -------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copiar el descriptor del proyecto y descargar dependencias (para cachear capas)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copiar el código fuente y empaquetar omitiendo los tests (los tests se ejecutan en CI)
COPY src ./src
RUN mvn clean package -DskipTests

# -------------------------------------------------------------
# FASE 2: Imagen final para ejecución
# -------------------------------------------------------------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Crear un usuario no privilegiado por seguridad
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

# Copiar el ejecutable generado en la fase previa
COPY --from=build /app/target/*.jar app.jar

# Exponer el puerto de Spring Boot
EXPOSE 8080

# Parámetros de ejecución optimizados para contenedores JVM con recursos limitados (Render 512MB RAM)
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=75.0", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]