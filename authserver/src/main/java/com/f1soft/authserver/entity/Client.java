package com.f1soft.authserver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String clientId;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_authentication_methods", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "authentication_method")
    private Set<String> authenticationMethods;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_authorization_grant_types", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "grant_type")
    private Set<String> authorizationGrantTypes;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_redirect_uris", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "redirect_uri")
    private Set<String> redirectUris;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "client_scopes", joinColumns = @JoinColumn(name = "client_id"))
    @Column(name = "scope")
    private Set<String> scopes;
}