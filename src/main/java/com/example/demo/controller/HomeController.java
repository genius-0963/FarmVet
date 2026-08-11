package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class HomeController {

    @GetMapping
    public String home() {
        return "FarmVet backend is running. Use the API under /api/";
    }

    @GetMapping("api")
    public String apiInfo() {
        return "FarmVet API is available. Use endpoints like /api/auth/register, /api/auth/login, /api/crops, /api/pesticides, /api/veterinarians";
    }
}
