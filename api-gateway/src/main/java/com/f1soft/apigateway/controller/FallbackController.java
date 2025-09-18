package com.f1soft.apigateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/fallback/auth")
    public ResponseEntity<String> authFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Auth service is currently unavailable. Please try again later.");
    }

    @GetMapping("/fallback/client")
    public ResponseEntity<String> clientFallback() {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("Client service is currently unavailable. Please try again later.");
    }
}
