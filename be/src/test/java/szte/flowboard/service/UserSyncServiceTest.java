package szte.flowboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSyncServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private UserSyncService userSyncService;

    private UserEntity testUser;
    private UserRepresentation keycloakUser;
    private UUID userId;
    private String keycloakId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        keycloakId = "keycloak-id-123";

        testUser = new UserEntity();
        testUser.setId(userId);
        testUser.setKeycloakId(keycloakId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmailAddress("john.doe@example.com");

        keycloakUser = new UserRepresentation();
        keycloakUser.setId(keycloakId);
        keycloakUser.setFirstName("John");
        keycloakUser.setLastName("Doe");
        keycloakUser.setEmail("john.doe@example.com");
        keycloakUser.setUsername("johndoe");

        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaims()).thenReturn(Map.of("sub", keycloakId));
    }

    @Test
    void testSyncUserFromKeycloak_UserAlreadyExists_ReturnsExistingUser() {
        // Given
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));

        // When
        UserEntity result = userSyncService.syncUserFromKeycloak(authentication);

        // Then
        assertNotNull(result);
        assertEquals(testUser.getId(), result.getId());
        verify(keycloakService, never()).getUserById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSyncUserFromKeycloak_KeycloakUserNotFound_ReturnsNull() {
        // Given
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(keycloakService.getUserById(keycloakId)).thenReturn(Optional.empty());

        // When
        UserEntity result = userSyncService.syncUserFromKeycloak(authentication);

        // Then
        assertNull(result);
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSyncUserFromKeycloak_Success_CreatesNewUser() {
        // Given
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(keycloakService.getUserById(keycloakId)).thenReturn(Optional.of(keycloakUser));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        // When
        UserEntity result = userSyncService.syncUserFromKeycloak(authentication);

        // Then
        assertNotNull(result);
        assertEquals(keycloakId, result.getKeycloakId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.doe@example.com", result.getEmailAddress());
        verify(userRepository, times(1)).save(argThat(user -> 
            user.getKeycloakId().equals(keycloakId) &&
            user.getFirstName().equals("John") &&
            user.getLastName().equals("Doe") &&
            user.getEmailAddress().equals("john.doe@example.com")
        ));
    }

    @Test
    void testSyncUserFromKeycloak_WithNullFields_UsesDefaults() {
        // Given
        UserRepresentation kcUserWithNulls = new UserRepresentation();
        kcUserWithNulls.setId(keycloakId);
        kcUserWithNulls.setFirstName(null);
        kcUserWithNulls.setLastName(null);
        kcUserWithNulls.setEmail(null);
        kcUserWithNulls.setUsername("johndoe");

        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());
        when(keycloakService.getUserById(keycloakId)).thenReturn(Optional.of(kcUserWithNulls));
        when(userRepository.save(any(UserEntity.class))).thenAnswer(invocation -> {
            UserEntity user = invocation.getArgument(0);
            user.setId(userId);
            return user;
        });

        // When
        UserEntity result = userSyncService.syncUserFromKeycloak(authentication);

        // Then
        assertNotNull(result);
        assertEquals("", result.getFirstName());
        assertEquals("", result.getLastName());
        assertEquals("johndoe", result.getEmailAddress()); // Should use username when email is null
    }

    @Test
    void testSyncUserFromKeycloak_Exception_ReturnsNull() {
        // Given
        when(userRepository.findByKeycloakId(keycloakId)).thenThrow(new RuntimeException("Database error"));

        // When
        UserEntity result = userSyncService.syncUserFromKeycloak(authentication);

        // Then
        assertNull(result);
    }
}



