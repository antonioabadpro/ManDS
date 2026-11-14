# -------------------------------------------------------------
# FASE 0: Compilación estática de estilos CSS (Tailwind)
# -------------------------------------------------------------
FROM node:22-alpine AS frontend-build
WORKDIR /app

# 1. Copiar manifiestos Node e instalar dependencias exactas (para cachear capas)
COPY package.json package-lock.json tailwind.config.js ./
RUN npm ci

# 2. Copiar plantillas, scripts y entrada CSS para compilar y minificar Tailwind
COPY src/main/resources/templates ./src/main/resources/templates
COPY src/main/resources/static/js ./src/main/resources/static/js
COPY src/main/resources/static/css/input.css ./src/main/resources/static/css/input.css
RUN npm run build:css

# -------------------------------------------------------------
# FASE 1: Compilación de la aplicación
# -------------------------------------------------------------
FROM maven:3.9.6-eclipse-temurin-21-alpine AS build
WORKDIR /app

# 1. Copiar el descriptor del proyecto y descargar dependencias (para cachear capas)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copiar el código fuente, sobrescribir CSS compilado y empaquetar omitiendo los tests (los tests se ejecutan en CI)
COPY src ./src
COPY --from=frontend-build /app/src/main/resources/static/css/tailwind.min.css ./src/main/resources/static/css/tailwind.min.css
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
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:+UseSerialGC", "-XX:MaxRAMPercentage=50.0", "-XX:InitialRAMPercentage=40.0", "-XX:MaxMetaspaceSize=128m", "-XX:ReservedCodeCacheSize=48m", "-Xss512k", "-Duser.timezone=Europe/Madrid", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]