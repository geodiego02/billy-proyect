package com.billy.files.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

	// Renderiza la página principal con el formulario de subida
    @GetMapping("/")
    public String index() {
        return "index"; // Retorna el nombre de la plantilla Thymeleaf "index.html"
    }
}
