package com.f1soft.client.application.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class ResourceController {

    @GetMapping("/hello")
    public String helloWorld() {
//        throw new RuntimeException("Something went wrong");
        return "Hello World";
    }
}
