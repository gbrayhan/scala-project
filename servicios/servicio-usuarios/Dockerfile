
# Utilizar la imagen oficial de OpenJDK como base
FROM openjdk:17-jdk-alpine

# Instalar sbt y dependencias necesarias
RUN apk add --no-cache bash curl unzip git && \
    curl -L -o sbt.zip https://github.com/sbt/sbt/releases/download/v1.9.0/sbt-1.9.0.zip && \
    unzip sbt.zip && \
    rm sbt.zip && \
    mv sbt /usr/local/ && \
    ln -s /usr/local/sbt/bin/sbt /usr/local/bin/sbt

# Crear directorio de trabajo
WORKDIR /app

# Copiar archivos de build
COPY build.sbt .
COPY project/ ./project/
COPY src/ ./src/

# Descargar dependencias y compilar el proyecto
RUN sbt compile

# Compilar el proyecto para producción
RUN sbt assembly

# Exponer el puerto
EXPOSE 3000

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "target/scala-2.13/servicio-usuarios-assembly-0.1.0.jar"]

