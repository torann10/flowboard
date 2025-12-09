package szte.flowboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import szte.flowboard.dto.response.UserResponse;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for managing users.
 * Handles user retrieval and authentication-related operations.
 * Extracts user information from Keycloak authentication tokens.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    /**
     * Retrieves a user entity based on the Keycloak ID from the authentication token.
     *
     * @param authentication the authentication object containing the JWT token
     * @return an Optional containing the user entity if found, empty otherwise
     */
    public Optional<UserEntity> getUserByAuthentication(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);

        return userRepository.findByKeycloakId(keycloakId);
    }

    /**
     * Retrieves all users in the system and converts them to UserResponse DTOs.
     *
     * @return a list of UserResponse DTOs for all users
     */
    public List<UserResponse> findAll() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getKeycloakId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmailAddress()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Extracts the Keycloak user ID (sub claim) from the JWT token.
     *
     * @param authentication the authentication object containing the JWT token
     * @return the Keycloak user ID as a string
     */
    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}