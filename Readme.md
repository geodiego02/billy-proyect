# Billy Proyect

Este proyecto consiste en una aplicación web en **Java 17** con **Spring Boot** para subir, segmentar y enviar archivos por correo.

## Características
- Subir archivos de cualquier tipo (.pdf, .png, .zip, etc.)
- Dividirlos en partes configurables en KB. Tiene límites de un mínimo de 16KB, máximo 1GB y número máximo de partes de 500 para garantizar el buen funcionamiento de la aplicación
- Enviar notificaciones de progreso en tiempo real (WebSocket)
- Envío de segmentos por correo (Spring Mail). Se usó una cuenta gmail con el correo billy.factura.chile@gmail.com.

## Requerimientos
- Java 17
- Maven 3.6+  
- Docker (opcional, para contenedor)

## Instalación y Ejecución
1. Clona este repositorio:
   git clone https://github.com/usuario/billy-proyect.git
   cd billy-proyect
2. Crear una imagen de docker:
   docker build -t billy-proyect .
3. Correr un contenedor para correr la aplicación:
   docker run -d --name billy-container -p 8080:8080 billy-proyect
4. Usar el endpoint localhost:8080 
5. Otra opción de ejecutar la aplicación es en la carpeta raíz del proyecto usar maven:
   mvn spring-boot:run
