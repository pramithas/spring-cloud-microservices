package com.f1soft.config_server.service;

import com.f1soft.config_server.entity.Client;
import com.f1soft.config_server.repository.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClientService {
    private final ClientRepository clientRepository;

    public Client getClient(String clientId) {
        return clientRepository.findByClientId(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Client not found: " + clientId));
    }
}
