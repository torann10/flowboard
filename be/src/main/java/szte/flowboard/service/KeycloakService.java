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

    private Keycloak keycloak;
    private RealmResource realmResource;

    @PostConstruct
    public void initKeycloak() {
        try {
            keycloak = KeycloakBuilder.builder()
                    .serverUrl(serverUrl)
                    .realm("master")
                    .username(adminUsername)
                    .password(adminPassword)
                    .clientId(clientId)
                    .build();

            realmResource = keycloak.realm(realm);
            log.info("Keycloak admin client initialized successfully");
        } catch (Exception e) {
            log.error("Failed to initialize Keycloak admin client", e);
            throw new RuntimeException("Failed to initialize Keycloak", e);
        }
    }

    public String createUser(String email, String firstName, String lastName, String password) {
        try {
            UsersResource usersResource = realmResource.users();

            List<UserRepresentation> existingUsers = usersResource.search(email);
            if (!existingUsers.isEmpty()) {
                throw new RuntimeException("User with email " + email + " already exists");
            }

            UserRepresentation user = new UserRepresentation();
            user.setUsername(email);
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setEnabled(true);
            user.setEmailVerified(true);

            Response response = usersResource.create(user);
            
            if (response.getStatus() == 201) {
                String userId = extractUserIdFromLocation(response.getLocation().toString());

                setUserPassword(userId, password);
                
                log.info("User created successfully with ID: {}", userId);
                return userId;
            } else {
                log.error("Failed to create user. Status: {}, Response: {}", 
                         response.getStatus(), response.readEntity(String.class));
                throw new RuntimeException("Failed to create user in Keycloak");
            }
        } catch (Exception e) {
            log.error("Error creating user in Keycloak", e);
            throw new RuntimeException("Failed to create user: " + e.getMessage());
        }
    }

    public Optional<UserRepresentation> getUserByEmail(String email) {
        try {
            List<UserRepresentation> users = realmResource.users().search(email);
            return users.stream()
                    .filter(user -> email.equals(user.getEmail()))
                    .findFirst();
        } catch (Exception e) {
            log.error("Error finding user by email: {}", email, e);
            return Optional.empty();
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

    public void updateUser(String userId, String email, String firstName, String lastName) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            UserRepresentation user = userResource.toRepresentation();
            
            user.setEmail(email);
            user.setFirstName(firstName);
            user.setLastName(lastName);
            
            userResource.update(user);
            log.info("User updated successfully: {}", userId);
        } catch (Exception e) {
            log.error("Error updating user: {}", userId, e);
            throw new RuntimeException("Failed to update user: " + e.getMessage());
        }
    }

    public void deleteUser(String userId) {
        try {
            realmResource.users().delete(userId);
            log.info("User deleted successfully: {}", userId);
        } catch (Exception e) {
            log.error("Error deleting user: {}", userId, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    private void setUserPassword(String userId, String password) {
        try {
            UserResource userResource = realmResource.users().get(userId);
            
            CredentialRepresentation credential = new CredentialRepresentation();
            credential.setType(CredentialRepresentation.PASSWORD);
            credential.setValue(password);
            credential.setTemporary(false);
            
            userResource.resetPassword(credential);
            log.info("Password set for user: {}", userId);
        } catch (Exception e) {
            log.error("Error setting password for user: {}", userId, e);
            throw new RuntimeException("Failed to set user password: " + e.getMessage());
        }
    }

    private String extractUserIdFromLocation(String location) {
        // Extract user ID from location header
        // Location format: http://localhost:9090/admin/realms/flowboard/users/{userId}
        String[] parts = location.split("/");
        return parts[parts.length - 1];
    }

    public void resetUserPassword(String userId, String newPassword) {
        setUserPassword(userId, newPassword);
    }
}

