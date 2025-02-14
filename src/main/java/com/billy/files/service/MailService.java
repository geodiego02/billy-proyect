package com.billy.files.service;

import java.io.File;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {
	private static final Logger logger = LoggerFactory.getLogger(MailService.class);

	private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendSegmentsByEmail(String toEmail, List<String> segmentNames) throws MessagingException {
    	 logger.info("Iniciando envío de segmentos a {}", toEmail);

    	// Filtrar valores vacíos o nulos
        segmentNames.removeIf(s -> s == null || s.trim().isEmpty());
        
        // Verificar si queda alguno
        if (segmentNames.isEmpty()) {
        	logger.warn("No hay segmentos válidos para enviar a {}", toEmail);
            return "No hay segmentos válidos para enviar.";
        }
        
        // Validar y sanitizar cada nombre de archivo para prevenir inyección de rutas o caracteres maliciosos.
        for (int i = 0; i < segmentNames.size(); i++) {
            String sanitized = segmentNames.get(i).replaceAll("[\\\\/]+", "");
            segmentNames.set(i, sanitized);
        }
        
        // Creamos un mensaje con adjuntos
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(toEmail);
        helper.setSubject("Segmentos de archivo");
        helper.setText("Adjuntos los segmentos solicitados.");

        // Definir la ubicación segura de los archivos: se restringe a la carpeta "splitFiles" del directorio temporal.
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        if (!tempDir.exists()) {
            logger.warn("El directorio de segmentos {} no existe.", tempDir.getAbsolutePath());
            return "Error: No se encontraron segmentos para enviar.";
        }
        
        boolean atLeastOneAttachment = false;
        for (String segmentName : segmentNames) {
            File segmentFile = new File(tempDir, segmentName);
            if (segmentFile.exists() && segmentFile.isFile()) {
            	logger.debug("Adjuntando segmento: {}", segmentFile.getAbsolutePath());
                FileSystemResource fileResource = new FileSystemResource(segmentFile);
                helper.addAttachment(segmentFile.getName(), fileResource);
                atLeastOneAttachment = true;
            }else {
                logger.warn("El archivo de segmento {} no existe.", segmentFile.getAbsolutePath());
            }
        }
        
        if (!atLeastOneAttachment) {
            logger.warn("No se encontraron archivos de segmento para enviar al correo {}", toEmail);
            return "No se encontraron segmentos válidos para enviar.";
        }
        mailSender.send(mimeMessage);
        logger.info("Segmentos enviados con éxito a {}", toEmail);
        return "Segmentos enviados con éxito a " + toEmail;
    }
}
