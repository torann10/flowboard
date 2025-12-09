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

/**
 * Service for interacting with Keycloak admin API.
 * Handles user retrieval from Keycloak using the admin client.
 * Initializes the Keycloak admin client on startup.
 */
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

    /**
     * Initializes the Keycloak admin client after bean construction.
     * Connects to Keycloak using admin credentials and sets up the realm resource.
     *
     * @throws RuntimeException if initialization fails
     */
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

    /**
     * Retrieves a user from Keycloak by their user ID.
     *
     * @param userId the Keycloak user ID
     * @return an Optional containing the user representation if found, empty otherwise
     */
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

