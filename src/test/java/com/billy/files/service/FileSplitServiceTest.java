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

	// Inyecta el mock en la clase FileSplitService
    @InjectMocks
    private FileSplitService fileSplitService;

    // Este es el mock para SimpMessagingTemplate
    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @BeforeEach
    void setUp() {
        // Inicializa los @Mock y @InjectMocks
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSplitFileSmallFile() throws IOException {
        // Creamos un archivo temporal con contenido muy pequeño
        File tempFile = File.createTempFile("test-split-small", ".txt");
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("Hola Mundo"); // ~10 bytes
        }

        // Llamamos al método splitFile de forma directa (la asincronía no afecta en un test unitario)
        fileSplitService.splitFile(tempFile, "test-small.txt", 5, "sessionABC"); 
        // 5 KB => en la práctica no segmentará, pues el archivo es muy chico

        // Verificamos que haya notificación "DONE"
        verify(messagingTemplate, times(1))
            .convertAndSend("/topic/progress/" + "sessionABC", "DONE");
    }

    @Test
    void testSplitFileBiggerFile() throws IOException {
        // Archivo temporal más grande
        File tempFile = File.createTempFile("test-split-big", ".txt");
        try (FileWriter fw = new FileWriter(tempFile)) {
            // ~ 150 bytes
            for(int i = 0; i < 30; i++){
                fw.write("ABCDE");
            }
        }

        // SegmentSizeKB= 1 => ~1KB => generará varias partes
        fileSplitService.splitFile(tempFile, "test-big.txt", 1, "sessionXYZ");

        // Verificamos que, al final, se mande "DONE"
        verify(messagingTemplate, times(1))
            .convertAndSend("/topic/progress/" + "sessionXYZ", "DONE");
    }
}
