package com.billy.files.service;

import java.io.File;
import java.util.List;
import java.util.regex.Pattern;

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
	private final InputSanitizationService inputSanitizationService;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");// Patrón para validar email

    public MailService(JavaMailSender mailSender, InputSanitizationService inputSanitizationService) {
        this.mailSender = mailSender;
        this.inputSanitizationService = inputSanitizationService;
    }

    public String sendSegmentsByEmail(String toEmail, List<String> segmentNames) throws MessagingException {
        validateEmail(toEmail);
        List<String> sanitizedSegments = inputSanitizationService.sanitizeSegmentNames(segmentNames);
        if (sanitizedSegments.isEmpty()) {
            logger.warn("No hay segmentos válidos para enviar a {}", toEmail);
            return "No hay segmentos válidos para enviar.";
        }
        MimeMessage mimeMessage = createMimeMessage(toEmail, sanitizedSegments);
        mailSender.send(mimeMessage);
        logger.info("Segmentos enviados con éxito a {}", toEmail);
        return "Segmentos enviados con éxito a " + toEmail;
    }
    
    private void validateEmail(String toEmail) {
        if (!EMAIL_PATTERN.matcher(toEmail).matches()) {
            logger.warn("Email inválido: {}", toEmail);
            throw new IllegalArgumentException("Error: Email inválido.");
        }
    }
    
    private MimeMessage createMimeMessage(String toEmail, List<String> segmentNames) throws MessagingException {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(toEmail);
        helper.setSubject("Segmentos de archivo");
        helper.setText("Adjuntos los segmentos solicitados.");
        
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        if (!tempDir.exists()) {
            logger.warn("El directorio de segmentos {} no existe.", tempDir.getAbsolutePath());
            throw new IllegalStateException("Error: No se encontraron segmentos para enviar.");
        }
        
        boolean atLeastOneAttachment = false;
        for (String segmentName : segmentNames) {
            File segmentFile = new File(tempDir, segmentName);
            if (segmentFile.exists() && segmentFile.isFile()) {
                logger.debug("Adjuntando segmento: {}", segmentFile.getAbsolutePath());
                FileSystemResource fileResource = new FileSystemResource(segmentFile);
                helper.addAttachment(segmentFile.getName(), fileResource);
                atLeastOneAttachment = true;
            } else {
                logger.warn("El archivo de segmento {} no existe.", segmentFile.getAbsolutePath());
            }
        }
        if (!atLeastOneAttachment) {
            logger.warn("No se encontraron archivos de segmento para enviar al correo {}", toEmail);
            throw new IllegalStateException("No se encontraron segmentos válidos para enviar.");
        }
        return mimeMessage;
    }
}
