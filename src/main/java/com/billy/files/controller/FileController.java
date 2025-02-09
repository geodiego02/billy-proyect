package com.billy.files.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.billy.files.service.FileSplitService;
import com.billy.files.service.MailService;

import jakarta.mail.MessagingException;

@RestController
public class FileController {

	private final FileSplitService fileSplitService;
    private final MailService mailService;
    
    public FileController(FileSplitService fileSplitService, MailService mailService) {
        this.fileSplitService = fileSplitService;
        this.mailService = mailService;
    }

    @PostMapping("/upload")
    public String uploadAndSplit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("segmentSizeKB") int segmentSizeKB,
            @RequestParam("sessionId") String sessionId) throws IllegalStateException, IOException {
    	
    	if (segmentSizeKB < 16 || segmentSizeKB > 1_048_576) {
    	    return "Error: El tamaño del segmento debe estar entre 16 KB y 1 GB.";
    	}

    	if (segmentSizeKB == 0) {
    	    return "Error: El tamaño de segmento no puede ser 0.";
    	}

        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }

        // Nombre original (sin ruta)
        String originalFilename = file.getOriginalFilename();

        // Copia local con prefijo
        File localCopy = new File(tempDir, "copy_" + System.currentTimeMillis() + "_" + originalFilename);
        file.transferTo(localCopy);

        // Llamar al servicio asíncrono pasando AMBOS: localCopy y originalFilename
        fileSplitService.splitFile(localCopy, originalFilename, segmentSizeKB, sessionId);
        return "Segmentación iniciada...";
    }

    @GetMapping("/download/{filename}")
    public ResponseEntity<InputStreamResource> downloadSegment(@PathVariable("filename") String filename) throws IOException {
        // Ajustar ruta a la carpeta temporal donde se guardan los segmentos
        File segmentFile = new File(System.getProperty("java.io.tmpdir") + "/splitFiles", filename);
        if (!segmentFile.exists()) {
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
    	
    	// Validar email
    	String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    	if (!toEmail.matches(emailRegex)) {
    	    return "Error: Email inválido.";
    	}
        mailService.sendSegmentsByEmail(toEmail, segmentNames);
        return "Segmentos enviados con éxito a " + toEmail;
    }
    
    @GetMapping("/listSegments")
    public List<String> listSegments(@RequestParam String originalName) {
        return fileSplitService.listSegments(originalName);
    }
}
