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
        // Preparar mock para MimeMessage
        MimeMessage mimeMessage = org.mockito.Mockito.mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        // Antes de llamar a mailService.sendSegmentsByEmail(...)
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        tempDir.mkdirs();
        try {
			new File(tempDir, "Form.pdf.0").createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        try {
			new File(tempDir, "Form.pdf.1").createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        // Llamar al método y capturar el valor retornado
        String result = mailService.sendSegmentsByEmail("destino@ejemplo.com",
                Arrays.asList("Form.pdf.0", "Form.pdf.1"));

        // Verificar que se invoque mailSender.send(mimeMessage)
        verify(mailSender).send(mimeMessage);
        // Verificar el mensaje retornado
        assertEquals("Segmentos enviados con éxito a destino@ejemplo.com", result);
    }
}
