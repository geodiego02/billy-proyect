package com.billy.files.controller;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.billy.files.exception.GlobalExceptionHandler;
import com.billy.files.service.FileSplitService;
import com.billy.files.service.InputSanitizationService;
import com.billy.files.service.MailService;

public class FileControllerTest {
	private static final Logger logger = LoggerFactory.getLogger(FileControllerTest.class);

    private MockMvc mockMvc;

    // Mockeamos los servicios que se usan en FileController.
    @Mock
    private FileSplitService fileSplitService;

    @Mock
    private MailService mailService;

    // Usamos @Spy para que se use la implementación real de InputSanitizationService.
    @Spy
    private InputSanitizationService inputSanitizationService = new InputSanitizationService();

    // * Dejamos que Mockito inyecte los mocks automáticamente sin instanciar manualmente.
    @InjectMocks
    private FileController fileController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Se agrega el ControllerAdvice para que se aplique el manejo global de excepciones
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
                "Contenido de prueba".getBytes()
        );

        // Simular que no lanza excepción al llamar splitFile(File, String, int, String)
        doNothing().when(fileSplitService)
            .splitFile(org.mockito.ArgumentMatchers.<java.io.File>any(), anyString(), anyInt(), anyString());

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
        logger.info("Iniciando testUpload_NoFile: se espera MissingServletRequestPartException porque no se envía el parámetro 'file'");
        // No se adjunta ningún archivo
        mockMvc.perform(
                multipart("/upload")
                    .param("segmentSizeKB", "16")
                    .param("sessionId", "abc123")
        )
        .andExpect(status().isBadRequest());
        logger.info("Finalización de testUpload_NoFile: error esperado capturado correctamente.");
    }

    @Test
    void testSendEmail_Success() throws Exception {
        // Simular que mailService retorna el mensaje de éxito.
        when(mailService.sendSegmentsByEmail(anyString(), anyList()))
                .thenReturn("Segmentos enviados con éxito a destino@ejemplo.com");

        mockMvc.perform(
                post("/sendEmail")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("toEmail", "destino@ejemplo.com")
                    // Simular varios nombres de segmentos
                    .param("segmentNames", "archivo1.pdf.0")
                    .param("segmentNames", "archivo1.pdf.1")
        )
        .andExpect(status().isOk());
    }
    
    @Test
    void testUpload_FileExceedsMaxSize() throws Exception {
        // Simular un archivo pequeño pero forzar el valor de getSize() para que exceda el límite.
        byte[] content = "pequeño contenido".getBytes();
        MockMultipartFile mockFile = new MockMultipartFile(
            "file", "bigfile.txt", MediaType.TEXT_PLAIN_VALUE, content);
        // Utilizamos un spy para modificar el valor de getSize()
        MockMultipartFile spyFile = org.mockito.Mockito.spy(mockFile);
        org.mockito.Mockito.doReturn(2L * 1_073_741_824L).when(spyFile).getSize(); // 2GB
        
        mockMvc.perform(
            multipart("/upload")
                .file(spyFile)
                .param("segmentSizeKB", "16")
                .param("sessionId", "abc123")
        )
        .andExpect(status().isOk())
        .andExpect(result -> {
            String response = result.getResponse().getContentAsString();
            assertThat(response, containsString("excede el tamaño máximo"));
        });
    }
    
    @Test
    void testSendEmail_InvalidEmail() throws Exception {
        mockMvc.perform(
            post("/sendEmail")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("toEmail", "email-invalido")
                .param("segmentNames", "archivo1.pdf.0")
        )
        .andExpect(status().isOk())
        .andExpect(result -> {
            String response = result.getResponse().getContentAsString();
            assertThat(response, containsString("Email inválido"));
        });
    }
    
    @Test
    void testListSegments_NoSegments() throws Exception {
        // Simular que el servicio retorna una lista vacía.
        when(fileSplitService.listSegments(anyString())).thenReturn(List.of());
        
        mockMvc.perform(
            get("/listSegments")
                .param("originalName", "archivoInexistente")
        )
        .andExpect(status().isOk())
        .andExpect(result -> {
            String response = result.getResponse().getContentAsString();
            assertThat(response, containsString("[]"));
        });
    }
}
