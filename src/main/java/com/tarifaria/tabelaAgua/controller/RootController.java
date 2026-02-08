package com.tarifaria.tabelaAgua.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class RootController {

    @GetMapping({"/"})
    public String root() {
        // Redireciona a raiz do servidor para a UI do Swagger
        return "redirect:/swagger-ui/index.html";
    }
}

