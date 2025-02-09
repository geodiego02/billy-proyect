package com.billy.files.controller;

import static org.hamcrest.CoreMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.billy.files.exception.GlobalExceptionHandler;
import com.billy.files.service.FileSplitService;
import com.billy.files.service.MailService;

public class FileControllerTest {

	private MockMvc mockMvc;

    @InjectMocks
    private FileController fileController;

    @Mock
    private FileSplitService fileSplitService;

    @Mock
    private MailService mailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Agregamos el ControllerAdvice para que el manejo de excepciones global esté activo
        mockMvc = MockMvcBuilders.standaloneSetup(fileController)
                    .setControllerAdvice(new GlobalExceptionHandler())
                    .build();
    }

    @Test
    void testUpload_Success() throws Exception {
        // Emular un archivo subido
        MockMultipartFile mockFile = new MockMultipartFile(
                "file",
                "prueba1.txt",
                MediaType.TEXT_PLAIN_VALUE,
                "Contenido de prueba".getBytes(StandardCharsets.UTF_8)
        );

        // Simular que NO lanza excepción al llamar splitFile(File, String, int, String)
        doNothing().when(fileSplitService)
        .splitFile(ArgumentMatchers.<File>any(), anyString(), anyInt(), anyString());

        // IMPORTANTE: Se usa un segmentSizeKB válido (>= 16)
        mockMvc.perform(
                multipart("/upload")
                    .file(mockFile)
                    .param("segmentSizeKB", "16")
                    .param("sessionId", "abc123")
        )
        .andExpect(status().isOk());
    }

    @Test
    void testUpload_NoFile() throws Exception {
        // No se adjunta ningún archivo
        mockMvc.perform(
                multipart("/upload")
                    .param("segmentSizeKB", "16")
                    .param("sessionId", "abc123")
        )
        .andExpect(status().isBadRequest());
    }

    @Test
    void testSendEmail_Success() throws Exception {
        // Simular que el mailService retorna el mensaje de éxito.
        when(mailService.sendSegmentsByEmail(anyString(), anyList()))
                .thenReturn("Segmentos enviados con éxito a destino@ejemplo.com");

        mockMvc.perform(
                post("/sendEmail")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("toEmail", "destino@ejemplo.com")
                    // Repetimos "segmentNames" para simular varios
                    .param("segmentNames", "archivo1.pdf.0")
                    .param("segmentNames", "archivo1.pdf.1")
        )
        .andExpect(status().isOk());
    }
}
