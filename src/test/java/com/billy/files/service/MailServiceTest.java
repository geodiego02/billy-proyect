package com.billy.files.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class MailServiceTest {
	@Mock
    private JavaMailSender mailSender;

    // * NUEVO: Usamos @Spy para InputSanitizationService
    @Spy
    private InputSanitizationService inputSanitizationService = new InputSanitizationService();

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendSegmentsByEmail() throws MessagingException, IOException {
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Preparar directorio y archivos para simular segmentos
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        tempDir.mkdirs();
        new File(tempDir, "Form.pdf.0").createNewFile();
        new File(tempDir, "Form.pdf.1").createNewFile();

        String result = mailService.sendSegmentsByEmail("destino@ejemplo.com",
                Arrays.asList("Form.pdf.0", "Form.pdf.1"));

        verify(mailSender).send(mimeMessage);
        assertEquals("Segmentos enviados con éxito a destino@ejemplo.com", result);
    }
}
