package com.billy.files.service;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.messaging.simp.SimpMessagingTemplate;

public class FileSplitServiceTest {

	@InjectMocks
    private FileSplitService fileSplitService;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    // Captura todos los mensajes enviados como segundo parámetro de convertAndSend
    @Captor
    private ArgumentCaptor<Object> messageCaptor;

    @BeforeEach
    void setUp() {
    	MockitoAnnotations.initMocks(this);
    }

    @Test
    void testSplitFileSmallFile() throws IOException {
        File tempFile = File.createTempFile("test-split-small", ".txt");
        try (FileWriter fw = new FileWriter(tempFile)) {
            fw.write("Hola Mundo");
        }

        fileSplitService.splitFile(tempFile, "test-small.txt", 5, "sessionABC");

        // Verificamos que se haya llamado al menos una vez a convertAndSend con el topic correcto
        verify(messagingTemplate, atLeastOnce())
            .convertAndSend(eq("/topic/progress/sessionABC"), messageCaptor.capture());

        // Obtenemos todos los mensajes enviados
        List<Object> allMessages = messageCaptor.getAllValues();

        // Comprobamos que al menos uno sea "DONE"
        boolean foundDone = allMessages.stream()
            .anyMatch(msg -> "DONE".equals(msg));
        assertTrue(foundDone, "No se encontró el mensaje DONE entre las llamadas de progreso");
    }

    @Test
    void testSplitFileBiggerFile() throws IOException {
        File tempFile = File.createTempFile("test-split-big", ".txt");
        try (FileWriter fw = new FileWriter(tempFile)) {
            for (int i = 0; i < 30; i++) {
                fw.write("ABCDE");
            }
        }

        fileSplitService.splitFile(tempFile, "test-big.txt", 1, "sessionXYZ");

        verify(messagingTemplate, atLeastOnce())
            .convertAndSend(eq("/topic/progress/sessionXYZ"), messageCaptor.capture());

        List<Object> allMessages = messageCaptor.getAllValues();
        boolean foundDone = allMessages.stream()
            .anyMatch(msg -> "DONE".equals(msg));
        assertTrue(foundDone, "No se encontró el mensaje DONE entre las llamadas de progreso");
    }
}
