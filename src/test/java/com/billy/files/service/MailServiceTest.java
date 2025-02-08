package com.billy.files.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public class MailServiceTest {

	@Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private MailService mailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendSegmentsByEmail() throws MessagingException {
        // Preparar Mock
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Llamar al método
        mailService.sendSegmentsByEmail("destino@ejemplo.com",
                Arrays.asList("Form.pdf.0", "Form.pdf.1"));

        // Verificar que se invoca mailSender.send(mimeMessage)
        verify(mailSender).send(mimeMessage);
    }
    
    // Podrías hacer verificaciones más completas,
    // p.e. capturar el MimeMessageHelper y chequear los attach, etc.
}
