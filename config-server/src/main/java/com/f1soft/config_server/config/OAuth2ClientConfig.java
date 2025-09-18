package com.f1soft.config_server.config;

import com.f1soft.config_server.entity.Client;
import com.f1soft.config_server.service.ClientService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OAuth2ClientConfig {

    private final ClientService clientService;

    public Client getClientDetails() {
        // Fetch client from DB
        return clientService.getClient("my-app-client");
    }
}

