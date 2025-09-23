package com.f1soft.authserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;

@Component
public class EnvLogger {

    @Autowired
    private Environment env;

    @PostConstruct
    public void logDbConfig() {
        System.out.println("DB URL: " + env.getProperty("spring.datasource.url"));
        System.out.println("DB USER: " + env.getProperty("spring.datasource.username"));
        System.out.println("DB PASSWORD: " + env.getProperty("spring.datasource.password"));
    }
}
