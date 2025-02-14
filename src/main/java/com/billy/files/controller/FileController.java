package com.billy.files.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.billy.files.service.FileSplitService;
import com.billy.files.service.InputSanitizationService;
import com.billy.files.service.MailService;

import jakarta.mail.MessagingException;

@RestController
public class FileController {
	private static final Logger logger = LoggerFactory.getLogger(FileController.class);
	private final FileSplitService fileSplitService;
    private final MailService mailService;
    private final InputSanitizationService inputSanitizationService;
    private static final long MAX_FILE_SIZE = 1_073_741_824L;
    
    public FileController(FileSplitService fileSplitService, MailService mailService, InputSanitizationService inputSanitizationService) {
        this.fileSplitService = fileSplitService;
        this.mailService = mailService;
        this.inputSanitizationService = inputSanitizationService;
    }

    @PostMapping("/upload")
    public String uploadAndSplit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("segmentSizeKB") int segmentSizeKB,
            @RequestParam("sessionId") String sessionId) throws IllegalStateException, IOException {
        
        logger.info("Recibido upload de archivo: {} | Tamaño de segmento: {} KB | SessionId: {}", 
                    file.getOriginalFilename(), segmentSizeKB, sessionId);

        if (segmentSizeKB < 16 || segmentSizeKB > 1_048_576) {
            logger.warn("Tamaño de segmento fuera de rango: {}", segmentSizeKB);
            return "Error: El tamaño del segmento debe estar entre 16 KB y 1 GB.";
        }
        if (segmentSizeKB == 0) {
            logger.warn("Tamaño de segmento es 0");
            return "Error: El tamaño de segmento no puede ser 0.";
        }
        // Validación del tamaño del archivo para evitar sobrecarga en el servidor
        if (file.getSize() > MAX_FILE_SIZE) {
            logger.warn("El archivo excede el tamaño máximo permitido: {} bytes", file.getSize());
            return "Error: El archivo excede el tamaño máximo permitido.";
        }

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        if (!tempDir.exists()) {
            boolean created = tempDir.mkdirs();
            if (created) {
                logger.info("Directorio temporal creado: {}", tempDir.getAbsolutePath());
            } else {
                logger.error("No se pudo crear el directorio temporal: {}", tempDir.getAbsolutePath());
                return "Error: No se pudo crear el directorio temporal.";
            }
        }

        // Obtener y sanitizar el nombre del archivo original y el sessionId
        String originalFilename = file.getOriginalFilename();
        originalFilename = inputSanitizationService.sanitizeFilename(originalFilename);
        sessionId = inputSanitizationService.sanitizeSessionId(sessionId);

        // Copia local con prefijo
        File localCopy = new File(tempDir, "copy_" + System.currentTimeMillis() + "_" + originalFilename);
        file.transferTo(localCopy);
        logger.debug("Archivo transferido a: {}", localCopy.getAbsolutePath());

        // Llamar al servicio asíncrono pasando localCopy y originalFilename sanitizado
        fileSplitService.splitFile(localCopy, originalFilename, segmentSizeKB, sessionId);
        return "Segmentación iniciada...";
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> downloadSegment(@PathVariable("filename") String filename) throws IOException {
    	logger.info("Solicitud de descarga para el segmento: {}", filename);
        // Ajustar ruta a la carpeta temporal donde se guardan los segmentos
        File segmentFile = new File(System.getProperty("java.io.tmpdir") + "/splitFiles", filename);
        if (!segmentFile.exists()) {
        	logger.warn("Archivo no encontrado: {}", segmentFile.getAbsolutePath());
            return ResponseEntity.notFound().build();
        }

        FileInputStream fis = new FileInputStream(segmentFile);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentLength(segmentFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(fis));
    }

    @PostMapping("/sendEmail")
    public String sendSegmentsByEmail(@RequestParam("toEmail") String toEmail,
                                      @RequestParam("segmentNames") List<String> segmentNames) throws MessagingException {
    	logger.info("Solicitud de envío de email a: {}", toEmail);
    	
    	// Validar email
    	String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    	if (!toEmail.matches(emailRegex)) {
    		 logger.warn("Email inválido: {}", toEmail);
    	    return "Error: Email inválido.";
    	}
        mailService.sendSegmentsByEmail(toEmail, segmentNames);
        return "Segmentos enviados con éxito a " + toEmail;
    }
    
    @GetMapping("/listSegments")
    public List<String> listSegments(@RequestParam String originalName) {
    	logger.info("Listando segmentos para el archivo original: {}", originalName);
        return fileSplitService.listSegments(originalName);
    }
}
