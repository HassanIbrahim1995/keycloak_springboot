package com.example.demokeycloakspring.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/demo")
public class Controller {

    @GetMapping
    @PreAuthorize("hasAnyRole('client-user')")
    public String hello() {
        return "Hello from springboot & keycloack -User-Role";
    }

    @GetMapping("/hello-admin")
    @PreAuthorize("hasAnyRole('client-admin')")
    public String hello2() {
        return "Hello from springboot & keycloack -Admin-Role";
    }

    @GetMapping("/hello-all")
    public String hello3() {
        return "Hello from springboot & keycloack all";
    }

}

