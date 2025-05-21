package com.example.bookingservice.util;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class KeycloakProvider {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.realm.master}")
    private String realmMaster;

    @Value("${keycloak.admin.client-id}")
    private String adminCli;

    @Value("${keycloak.admin.username}")
    private String userConsole;

    @Value("${keycloak.admin.password}")
    private String passwordConsole;

    @Value("${keycloak.admin.client-secret}")
    private String clientSecret;

    private Keycloak keycloakInstance;

    @PostConstruct
    public void init() {
        this.keycloakInstance = KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm(realmMaster)
                .clientId(adminCli)
                .username(userConsole)
                .password(passwordConsole)
                .clientSecret(clientSecret)
                .resteasyClient(new ResteasyClientBuilderImpl()
                        .connectionPoolSize(10)
                        .build())
                .build();
    }

    public RealmResource getRealmResource() {
        if (keycloakInstance == null) {
            throw new IllegalStateException("Keycloak client has not been initialized.");
        }
        return keycloakInstance.realm(realm);
    }

    public UsersResource getUsersResource() {
        return getRealmResource().users();
    }

    @PreDestroy
    public void close() {
        if (keycloakInstance != null) {
            keycloakInstance.close();
        }
    }
}