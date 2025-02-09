package com.billy.files.service;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class FileSplitServiceTest {

	@InjectMocks
    private FileSplitService fileSplitService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSplitFileSmallFile() throws IOException {
        // Crear un archivo temporal con contenido pequeño
        File tempFile = File.createTempFile("test-split-small", ".txt");
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("Hola Mundo");
        }

        // Llamar al método splitFile (la asincronía no afecta en test unitario)
        fileSplitService.splitFile(tempFile, "test-small.txt", 5, "sessionABC");

        // Verificar que se envía la notificación "DONE"
        verify(messagingTemplate, times(1))
            .convertAndSend("/topic/progress/" + "sessionABC", "DONE");
    }

    @Test
    void testSplitFileBiggerFile() throws IOException {
        // Archivo temporal mayor
        File tempFile = File.createTempFile("test-split-big", ".txt");
        try (FileWriter fw = new FileWriter(tempFile)) {
            for (int i = 0; i < 30; i++) {
                fw.write("ABCDE");
            }
        }

        // Usar segmentSizeKB=1 para generar varias partes
        fileSplitService.splitFile(tempFile, "test-big.txt", 1, "sessionXYZ");

        // Verificar que se envía la notificación "DONE"
        verify(messagingTemplate, times(1))
            .convertAndSend("/topic/progress/" + "sessionXYZ", "DONE");
    }
}
