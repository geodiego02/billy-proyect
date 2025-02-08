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

@RestController
public class FileController {

	@Autowired
    private FileSplitService fileSplitService;

    @Autowired
    private MailService mailService;

    /**
     * Sube un archivo y llama al servicio para segmentar.
     * @param file Archivo subido
     * @param segmentSizeKB Tamaño del segmento en KB
     * @param sessionId Identificador de sesión (para WebSocket)
     */
    @PostMapping("/upload")
    public String uploadAndSplit(
            @RequestParam("file") MultipartFile file,
            @RequestParam("segmentSizeKB") int segmentSizeKB,
            @RequestParam("sessionId") String sessionId) {
    	
    	if (segmentSizeKB < 16 || segmentSizeKB > 1_048_576) {
    	    return "Error: El tamaño del segmento debe estar entre 16 KB y 1 GB.";
    	}

    	if (segmentSizeKB == 0) {
    	    return "Error: El tamaño de segmento no puede ser 0.";
    	}


        try {
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
        } catch (IOException e) {
            e.printStackTrace();
            return "Error al procesar el archivo: " + e.getMessage();
        }
    }


    /**
     * Descarga de un segmento específico
     */
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

    /**
     * Envío de segmentos por correo. 
     * Se supone que el front-end ya sabe qué partes se han generado y pasa la lista de nombres.
     */
    @PostMapping("/sendEmail")
    public String sendSegmentsByEmail(@RequestParam("toEmail") String toEmail,
                                      @RequestParam("segmentNames") List<String> segmentNames) {
    	
    	// Validar email
    	String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
    	if (!toEmail.matches(emailRegex)) {
    	    return "Error: Email inválido.";
    	}

    	
    	// Filtrar valores vacíos o nulos
        List<String> filteredSegments = segmentNames.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .collect(Collectors.toList());

        // Verificar si queda alguno
        if (filteredSegments.isEmpty()) {
            return "No hay segmentos válidos para enviar.";
        }
    	
        try {
            mailService.sendSegmentsByEmail(toEmail, filteredSegments);
            return "Segmentos enviados con éxito a " + toEmail;
        } catch (Exception e) {
            e.printStackTrace();
            return "Error al enviar email: " + e.getMessage();
        }
    }
    
    
    @GetMapping("/listSegments")
    public List<String> listSegments(@RequestParam String originalName) {
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "splitFiles");
        File[] files = tempDir.listFiles();

        List<String> segmentNames = new ArrayList<>();
        if (files != null) {
            for (File f : files) {
                if (f.getName().startsWith(originalName + ".")) {
                    segmentNames.add(f.getName());
                }
            }
        }
        
        // Ordena los nombres por la parte numérica tras el último punto
        segmentNames.sort((a, b) -> {
            // Extraer la última parte después del punto. Ej: "ojoDerecho.pdf.14" -> "14"
            int numA = extraerParteNumerica(a);
            int numB = extraerParteNumerica(b);
            return Integer.compare(numA, numB);
        });

        return segmentNames;
    }

    private int extraerParteNumerica(String fileName) {
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot == -1) {
            return 0; // o alguna forma de manejar si no hay parte numérica
        }
        try {
            return Integer.parseInt(fileName.substring(lastDot + 1));
        } catch (NumberFormatException e) {
            return 0; 
        }
    }
}
