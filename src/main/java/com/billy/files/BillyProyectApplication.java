package com.billy.files;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class BillyProyectApplication {
	private static final Logger logger = LoggerFactory.getLogger(BillyProyectApplication.class);

	public static void main(String[] args) {
		crearCarpetaLogs();
        logger.info("Iniciando la aplicación BillyProyectApplication...");
		SpringApplication.run(BillyProyectApplication.class, args);
	}

	private static void crearCarpetaLogs() {
        File logDir = new File("logs");
        if (!logDir.exists()) {
            boolean created = logDir.mkdirs();
            if (created) {
                System.out.println("Carpeta 'logs' creada exitosamente.");
            } else {
                System.err.println("No se pudo crear la carpeta 'logs'.");
            }
        }
    }
}
