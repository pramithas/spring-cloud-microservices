package com.f1soft.authserver.service;

import com.f1soft.authserver.entity.Client;
import com.f1soft.authserver.repository.ClientRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class ClientService implements RegisteredClientRepository {

    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Override
    public void save(RegisteredClient registeredClient) {
        // Map back from RegisteredClient -> Client entity
//        Client client = new Client();
//        client.setId(registeredClient.getId());
//        client.setClientId(registeredClient.getClientId());
//        client.setClientSecret(registeredClient.getClientSecret());
//        client.setAuthenticationMethods(
//                registeredClient.getClientAuthenticationMethods().stream()
//                        .map(ClientAuthenticationMethod::getValue)
//                        .toList()
//        );
//        client.setAuthorizationGrantTypes(
//                registeredClient.getAuthorizationGrantTypes().stream()
//                        .map(AuthorizationGrantType::getValue)
//                        .toList()
//        );
//        client.setRedirectUris(registeredClient.getRedirectUris().stream().toList());
//        client.setScopes(registeredClient.getScopes().stream().toList());
//
//        clientRepository.save(client);
    }

    @Override
    public RegisteredClient findById(String id) {
        System.out.println("Find Client by id: " + id);
        return clientRepository.findById(id)
                .map(this::convertToRegisteredClient)
                .orElse(null);
    }

    @Override
    public RegisteredClient findByClientId(String clientId) {
        System.out.println("Find Client by clientId: " + clientId);
        return clientRepository.findByClientId(clientId)
                .map(this::convertToRegisteredClient)
                .orElse(null);
    }

    private RegisteredClient convertToRegisteredClient(Client client) {
        return RegisteredClient.withId(client.getId())
                .clientId(client.getClientId())
                .clientAuthenticationMethods(methods ->
                        client.getAuthenticationMethods().forEach(
                                method -> methods.add(new ClientAuthenticationMethod(method))
                        )
                )
                .authorizationGrantTypes(grants ->
                        client.getAuthorizationGrantTypes().forEach(
                                grant -> grants.add(new AuthorizationGrantType(grant))
                        )
                )
                .redirectUris(uris -> uris.addAll(client.getRedirectUris()))
                .scopes(scopes -> scopes.addAll(client.getScopes()))
                .build();
    }
}
