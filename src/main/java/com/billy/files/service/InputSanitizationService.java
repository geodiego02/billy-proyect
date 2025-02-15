package com.billy.files.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

@Service
public class InputSanitizationService {

	public String sanitizeFilename(String filename) {
        if (filename == null) {
            return "archivo_desconocido";
        }
        // Elimina separadores de ruta (backslash y forward slash)
        filename = filename.replaceAll("[\\\\/]+", "");
        // Elimina caracteres que no sean alfanuméricos, punto, guion o guion bajo
        filename = filename.replaceAll("[^a-zA-Z0-9._-]", "");
        // Limita la longitud del nombre a 100 caracteres
        return filename.length() > 100 ? filename.substring(0, 100) : filename;
    }

    public String sanitizeSessionId(String sessionId) {
        if (sessionId == null) {
            return "";
        }
        // Permite solo caracteres alfanuméricos y guiones
        return sessionId.replaceAll("[^a-zA-Z0-9-]", "");
    }
    
    public List<String> sanitizeSegmentNames(List<String> segmentNames) {
        return segmentNames.stream()
                .filter(s -> s != null && !s.trim().isEmpty())
                .map(s -> s.replaceAll("[\\\\/]+", ""))
                .collect(Collectors.toList());
    }
}
