# Usar una imagen de Java oficial
FROM openjdk:17-jdk-slim

# Crear un directorio para la app
WORKDIR /app

# Copiar el JAR
COPY target/*.jar app.jar

# Exponer el puerto (depende de tu app, normal 8080)
EXPOSE 8080

# Comando para ejecutar el JAR
ENTRYPOINT ["java", "-jar", "app.jar"]
