package szte.flowboard.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import szte.flowboard.dto.response.UserResponse;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.UserRepository;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private UserService userService;

    private UserEntity testUser;
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
    }

    @Test
    void testGetUserByAuthentication_Success() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaims()).thenReturn(Map.of("sub", keycloakId));
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.of(testUser));

        // When
        Optional<UserEntity> result = userService.getUserByAuthentication(authentication);

        // Then
        assertTrue(result.isPresent());
        assertEquals(testUser.getId(), result.get().getId());
        assertEquals(keycloakId, result.get().getKeycloakId());
        verify(userRepository, times(1)).findByKeycloakId(keycloakId);
    }

    @Test
    void testGetUserByAuthentication_NotFound_ReturnsEmpty() {
        // Given
        when(authentication.getPrincipal()).thenReturn(jwt);
        when(jwt.getClaims()).thenReturn(Map.of("sub", keycloakId));
        when(userRepository.findByKeycloakId(keycloakId)).thenReturn(Optional.empty());

        // When
        Optional<UserEntity> result = userService.getUserByAuthentication(authentication);

        // Then
        assertTrue(result.isEmpty());
    }

    @Test
    void testFindAll_Success() {
        // Given
        UserEntity user2 = new UserEntity();
        user2.setId(UUID.randomUUID());
        user2.setKeycloakId("keycloak-id-456");
        user2.setFirstName("Jane");
        user2.setLastName("Smith");
        user2.setEmailAddress("jane.smith@example.com");

        when(userRepository.findAll()).thenReturn(List.of(testUser, user2));

        // When
        List<UserResponse> result = userService.findAll();

        // Then
        assertEquals(2, result.size());
        assertEquals(testUser.getId(), result.get(0).id());
        assertEquals(testUser.getKeycloakId(), result.get(0).keycloakId());
        assertEquals(testUser.getFirstName(), result.get(0).firstName());
        assertEquals(testUser.getLastName(), result.get(0).lastName());
        assertEquals(testUser.getEmailAddress(), result.get(0).emailAddress());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void testFindAll_EmptyList_ReturnsEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        // When
        List<UserResponse> result = userService.findAll();

        // Then
        assertTrue(result.isEmpty());
    }
}



