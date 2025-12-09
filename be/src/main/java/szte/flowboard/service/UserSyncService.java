package szte.flowboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.UserRepository;

import java.util.Optional;

/**
 * Service for synchronizing users from Keycloak to the local database.
 * Creates local user entities based on Keycloak user information when users first authenticate.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    /**
     * Synchronizes a user from Keycloak to the local database.
     * If the user already exists locally, returns the existing user.
     * Otherwise, fetches user data from Keycloak and creates a new local user entity.
     *
     * @param authentication the authentication object containing the JWT token
     * @return the synchronized user entity, or null if synchronization fails
     */
    @Transactional
    public UserEntity syncUserFromKeycloak(Authentication authentication) {
        try {
            var keycloakId = (String) ((Jwt)authentication.getPrincipal()).getClaims().get("sub");

            Optional<UserEntity> existingUser = userRepository.findByKeycloakId(keycloakId);

            if (existingUser.isPresent()) {
                return existingUser.get();
            }

            Optional<UserRepresentation> keycloakUser = keycloakService.getUserById(keycloakId);
            UserEntity userEntity = getUserEntity(keycloakUser, keycloakId);

            if(userEntity == null) {
                return null;
            }

            UserEntity savedUser = userRepository.save(userEntity);
            log.info("User synchronized from Keycloak: {}", savedUser.getId());
            
            return savedUser;
        } catch (Exception e) {
            log.error("Error synchronizing user from Keycloak", e);
            return null;
        }
    }

    /**
     * Converts a Keycloak user representation to a local user entity.
     *
     * @param keycloakUser the Keycloak user representation
     * @param keycloakId the Keycloak user ID
     * @return a user entity created from Keycloak data, or null if user not found in Keycloak
     */
    private static UserEntity getUserEntity(Optional<UserRepresentation> keycloakUser, String keycloakId) {
        if (keycloakUser.isEmpty()) {
            log.warn("User not found in Keycloak: {}", keycloakId);
            return null;
        }

        UserRepresentation kcUser = keycloakUser.get();

        UserEntity userEntity = new UserEntity();
        userEntity.setKeycloakId(keycloakId);
        userEntity.setFirstName(kcUser.getFirstName() != null ? kcUser.getFirstName() : "");
        userEntity.setLastName(kcUser.getLastName() != null ? kcUser.getLastName() : "");
        userEntity.setEmailAddress(kcUser.getEmail() != null ? kcUser.getEmail() : kcUser.getUsername());
        return userEntity;
    }
}

