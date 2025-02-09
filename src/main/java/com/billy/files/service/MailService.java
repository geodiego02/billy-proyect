package com.billy.files.service;

import java.io.File;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class MailService {

	private final JavaMailSender mailSender;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public String sendSegmentsByEmail(String toEmail, List<String> segmentNames) throws MessagingException {

    	// Filtrar valores vacíos o nulos
        segmentNames.removeIf(s -> s == null || s.trim().isEmpty());
        
        // Verificar si queda alguno
        if (segmentNames.isEmpty()) {
            return "No hay segmentos válidos para enviar.";
        }
        // Creamos un mensaje con adjuntos
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
        helper.setTo(toEmail);
        helper.setSubject("Segmentos de archivo");
        helper.setText("Adjuntos los segmentos solicitados.");

        // Adjuntamos cada segmento
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        for (String segmentName : segmentNames) {
            File segmentFile = new File(tempDir, segmentName);
            if (segmentFile.exists()) {
                FileSystemResource fileResource = new FileSystemResource(segmentFile);
                helper.addAttachment(segmentFile.getName(), fileResource);
            }
        }
        mailSender.send(mimeMessage);
        return "Segmentos enviados con éxito a " + toEmail;
    }
}
