package szte.flowboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import jakarta.ws.rs.core.Response;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class KeycloakService {

    @Value("${keycloak.auth-server-url}")
    private String serverUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.admin.client-id}")
    private String clientId;

    @Value("${keycloak.admin.username}")
    private String adminUsername;

    @Value("${keycloak.admin.password}")
    private String adminPassword;

    private RealmResource realmResource;

    @PostConstruct
    public void initKeycloak() {
        try {
            try (var keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId(clientId)
                    .build()) {

                realmResource = keycloak.realm(realm);
            }
            log.info("Keycloak admin client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak admin client", e);
            throw new RuntimeException("Failed to initialize Keycloak", e);
        }
    }

    public Optional<UserRepresentation> getUserById(String userId) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            return Optional.of(user);
        } catch (Exception e) {
            log.error("Error finding user by ID: {}", userId, e);
            return Optional.empty();
        }
    }
}

