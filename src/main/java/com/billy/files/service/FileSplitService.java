package com.billy.files.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class FileSplitService {
	private static final Logger logger = LoggerFactory.getLogger(FileSplitService.class);

    private final SimpMessagingTemplate messagingTemplate;

    public FileSplitService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Async
    public void splitFile(File localFile, String originalFilename, int segmentSizeKB, String sessionId) throws IOException {
        logger.info("Iniciando la segmentación del archivo: {}", originalFilename);
        
        // Validar que el archivo exista y tenga contenido
        if (!localFile.exists() || localFile.length() == 0) {
            logger.error("El archivo {} no existe o está vacío. Abortando la segmentación.", localFile.getAbsolutePath());
            messagingTemplate.convertAndSend("/topic/progress/" + sessionId, "0.00% - Segmentación incompleta. Por favor, inténtelo de nuevo.");
            return;
        }
        
        // Elimina los segmentos viejos del mismo archivo
        File tempDir = localFile.getParentFile();
        File[] oldFiles = tempDir.listFiles();
        if (oldFiles != null) {
            for (File f : oldFiles) {
                if (f.getName().startsWith(originalFilename + ".")) {
                    logger.debug("Eliminando segmento antiguo: {}", f.getName());
                    if (!f.delete()) { // Verificar eliminación
                        logger.warn("No se pudo eliminar el archivo antiguo: {}", f.getAbsolutePath());
                    }
                }
            }
        }
        
        long segmentSize = (long) segmentSizeKB * 1024L;
        long totalBytes = localFile.length();
        long position = 0;  // Bytes procesados
        int partNumber = 0;
        ByteBuffer buffer = ByteBuffer.allocate(8 * 1024); // 8KB buffer
        
        try (FileChannel inChannel = FileChannel.open(localFile.toPath())) {
            while (position < totalBytes) {
                String partName = originalFilename + "." + partNumber;
                File outFile = new File(tempDir, partName);
                logger.debug("Creando segmento: {}", outFile.getAbsolutePath());
                try (FileOutputStream fos = new FileOutputStream(outFile);
                     FileChannel outChannel = fos.getChannel()) {
                    long bytesToRead = segmentSize;
                    while (bytesToRead > 0) {
                        int bytesRead = inChannel.read(buffer);
                        if (bytesRead == -1) break;
                        buffer.flip();
                        outChannel.write(buffer);
                        buffer.clear();
                        position += bytesRead;
                        bytesToRead -= bytesRead;
                        
                        // Calcular y enviar progreso intermedio
                        double progress = (double) position / totalBytes * 100.0;
                        messagingTemplate.convertAndSend("/topic/progress/" + sessionId, String.format("%.2f%%", progress));
                    }
                }
                partNumber++;
            }
        } catch (IOException e) {
            logger.error("Error al segmentar el archivo {}: {}", originalFilename, e.getMessage(), e);
            throw e;
        } finally {
            // Intentar eliminar la copia local del archivo
            if (localFile.exists() && !localFile.delete()) {
                logger.warn("No se pudo eliminar el archivo local: {}", localFile.getAbsolutePath());
            }
        }
        
        //* Umbral para considerar que la segmentación se completó
        long umbral = 10; // 10 bytes de diferencia se consideran insignificantes
        double finalProgress;
        if (Math.abs(totalBytes - position) < umbral) {
            finalProgress = 100.00;
        } else {
            finalProgress = (double) position / totalBytes * 100.0;
        }
        
        if (finalProgress < 100.00) {
            messagingTemplate.convertAndSend("/topic/progress/" + sessionId, String.format("%.2f%% - Segmentación incompleta. Por favor, inténtelo de nuevo.", finalProgress));
        } else {
        	messagingTemplate.convertAndSend("/topic/progress/" + sessionId, "DONE");
        }
        logger.info("Segmentación finalizada para el archivo: {}", originalFilename);
    }
    
    public List<String> listSegments(String originalName) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        File[] files = tempDir.listFiles();
        List<String> segmentNames = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (f.getName().startsWith(originalName + ".")) {
                    segmentNames.add(f.getName());
                }
            }
        }
        // Ordenar por la parte numérica tras el último punto.
        segmentNames.sort((a, b) -> {
            int numA = extractNumericPart(a);
            int numB = extractNumericPart(b);
            return Integer.compare(numA, numB);
        });
        logger.info("Se encontraron {} segmentos para el archivo {}", segmentNames.size(), originalName);
        return segmentNames;
    }
    
    private int extractNumericPart(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return 0;
        }
        try {
            return Integer.parseInt(fileName.substring(lastDot + 1));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
