# Usar una imagen base con Java 17 (por ejemplo, Eclipse Temurin o Amazon Corretto)
FROM amazoncorretto:17-alpine

# Crear un directorio para la app
WORKDIR /app

# Copiar el JAR generado (ajusta el nombre si difiere)
COPY target/billy-proyect-0.0.1-SNAPSHOT.jar /app/billy-proyect.jar

# Exponer el puerto en el contenedor (ejemplo: 8080)
EXPOSE 8080

# Comando para ejecutar la app
ENTRYPOINT ["java", "-jar", "billy-proyect.jar"]
