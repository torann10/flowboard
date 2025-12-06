package szte.flowboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KeycloakServiceTest {

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private UserResource userResource;

    @InjectMocks
    private KeycloakService keycloakService;

    private UserRepresentation testUser;
    private String userId;

    @BeforeEach
    void setUp() {
        userId = "keycloak-user-id-123";

        testUser = new UserRepresentation();
        testUser.setId(userId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        testUser.setUsername("johndoe");

        // Set private fields using reflection to avoid @PostConstruct initialization
        ReflectionTestUtils.setField(keycloakService, "serverUrl", "http://localhost:9090");
        ReflectionTestUtils.setField(keycloakService, "realm", "flowboard");
        ReflectionTestUtils.setField(keycloakService, "clientId", "admin-cli");
        ReflectionTestUtils.setField(keycloakService, "adminUsername", "admin");
        ReflectionTestUtils.setField(keycloakService, "adminPassword", "password");
        ReflectionTestUtils.setField(keycloakService, "realmResource", realmResource);
    }

    @Test
    void testGetUserById_Success() {
        // Given
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(testUser);

        // When
        Optional<UserRepresentation> result = keycloakService.getUserById(userId);

        // Then
        assertTrue(result.isPresent());
        assertEquals(userId, result.get().getId());
        assertEquals("John", result.get().getFirstName());
        assertEquals("Doe", result.get().getLastName());
    }

    @Test
    void testGetUserById_Exception_ReturnsEmpty() {
        // Given
        when(realmResource.users()).thenReturn(usersResource);
        when(usersResource.get(userId)).thenReturn(userResource);
        when(userResource.toRepresentation()).thenThrow(new RuntimeException("User not found"));

        // When
        Optional<UserRepresentation> result = keycloakService.getUserById(userId);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetUserById_NullRealmResource_ReturnsEmpty() {
        // Given
        ReflectionTestUtils.setField(keycloakService, "realmResource", null);

        // When
        Optional<UserRepresentation> result = keycloakService.getUserById(userId);

        // Then
        assertTrue(result.isEmpty());
    }
}

