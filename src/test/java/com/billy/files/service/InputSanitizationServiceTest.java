package com.billy.files.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class InputSanitizationServiceTest {

	private InputSanitizationService service = new InputSanitizationService();

    @Test
    void testSanitizeFilename() {
        String original = "inva/lid\\name?.txt";
        String sanitized = service.sanitizeFilename(original);
        // Se espera que elimine separadores y caracteres no permitidos
        assertEquals("invalidname.txt", sanitized);
    }
    
    @Test
    void testSanitizeSessionId() {
        String original = "abc#123!";
        String sanitized = service.sanitizeSessionId(original);
        // Se espera que solo queden caracteres alfanuméricos y guiones
        assertEquals("abc123", sanitized);
    }
}
