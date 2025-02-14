package com.billy.files.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
	private static final Logger logger = LoggerFactory.getLogger(WebController.class);

	// Renderiza la página principal con el formulario de subida
    @GetMapping("/")
    public String index() {
    	logger.info("Accediendo a la página principal (index).");
        return "index"; // Retorna el nombre de la plantilla Thymeleaf "index.html"
    }
}
