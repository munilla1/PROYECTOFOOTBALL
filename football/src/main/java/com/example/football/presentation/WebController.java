package com.example.football.presentation;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {
    @GetMapping({"/", "/registro"})
    public String registro() {
        return "forward:/ui/registro.html";
    }

    @GetMapping("/login")
    public String login() {
        return "forward:/ui/login.html";
    }
}
