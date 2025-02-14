# Billy Proyect

Este proyecto consiste en una aplicación web en **Java 17** con **Spring Boot** para subir, segmentar y enviar archivos por correo.

## Características
- Subir archivos de cualquier tipo (.pdf, .png, .zip, etc.)
- Dividirlos en partes configurables en KB. Tiene límites de un mínimo de 16KB, máximo 1GB y número máximo de partes de 500 para garantizar el buen funcionamiento de la aplicación.
- Enviar notificaciones de progreso en tiempo real (WebSocket)
- Envío de segmentos por correo (Spring Mail). Se usó una cuenta gmail con el correo billy.factura.chile@gmail.com.

## Requerimientos
- Git, Maven 3.6+ y java 17. Docker es opcional para ejecutar como segunda opción. 

## Instalación y Ejecución
1. Abrir la linea de comandos.
2. Clonar el repositorio con este comando:
   git clone https://github.com/geodiego02/billy-proyect.git
2. Abrir la carpeta contenedora del repositorio:
   cd billy-proyect
3. Usar un comando maven en la linea de comandos para descargar las dependencias y compilar el proyecto:
   mvn clean install
4. Para ejecutar la aplicación se ocupa el siguiente comando:
   mvn spring-boot:run
5. Usar el endpoint localhost:8080 en cualquier navegador.
6. Opcional: Después de obtener la carpeta /target/ se puede crear una imagen de docker con el siguiente comando:
   docker build -t billy-proyect .
7. Opcional: Correr un contenedor para ejecutar la aplicación:
   docker run -d --name billy-container -p 8080:8080 billy-proyect
8. Usar el endpoint localhost:8080 
   
