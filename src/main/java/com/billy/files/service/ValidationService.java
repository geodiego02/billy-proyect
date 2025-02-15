package com.billy.files.service;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ValidationService {

	private static final Logger logger = LoggerFactory.getLogger(ValidationService.class);
    private static final long MAX_FILE_SIZE = 1_073_741_824L; // 1GB en bytes
    private static final int MIN_SEGMENT_SIZE = 16; // en KB
    private static final int MAX_SEGMENT_SIZE = 1_048_576; // 1GB en KB

    public void validateSegmentSize(int segmentSizeKB) {
        if (segmentSizeKB < MIN_SEGMENT_SIZE || segmentSizeKB > MAX_SEGMENT_SIZE) {
            logger.warn("Tamaño de segmento fuera de rango: {}", segmentSizeKB);
            throw new IllegalArgumentException("Error: El tamaño del segmento debe estar entre 16 KB y 1 GB.");
        }
        if (segmentSizeKB == 0) {
            logger.warn("Tamaño de segmento es 0");
            throw new IllegalArgumentException("Error: El tamaño del segmento no puede ser 0.");
        }
    }

    public void validateFileSize(long fileSize) {
        if (fileSize > MAX_FILE_SIZE) {
            logger.warn("El archivo excede el tamaño máximo permitido: {} bytes", fileSize);
            throw new IllegalArgumentException("Error: El archivo excede el tamaño máximo permitido.");
        }
    }

    public File ensureTempDirectoryExists(String dirPath) {
        File tempDir = new File(dirPath);
        if (!tempDir.exists()) {
            // Sincronización para asegurar la creación única del directorio en entornos multihilo
            synchronized (ValidationService.class) {
                if (!tempDir.exists()) { // Doble verificación
                    boolean created = tempDir.mkdirs();
                    if (!created) {
                        logger.error("No se pudo crear el directorio temporal: {}", tempDir.getAbsolutePath());
                        throw new IllegalStateException("Error: No se pudo crear el directorio temporal.");
                    } else {
                        logger.info("Directorio temporal creado: {}", tempDir.getAbsolutePath());
                    }
                }
            }
        }
        return tempDir;
    }
}
