# Usar una imagen base con Java 17 
FROM amazoncorretto:17-alpine

# Crear un directorio para la app
WORKDIR /app

# Crear la carpeta logs
RUN mkdir -p /app/logs

# Copiar el JAR generado 
COPY target/billy-proyect-0.0.1-SNAPSHOT.jar /app/billy-proyect.jar

# Exponer el puerto en el contenedor
EXPOSE 8080

# Definir un volumen para logs
VOLUME /app/logs

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "billy-proyect.jar"]
