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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserSyncService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    @Transactional
    public UserEntity syncUserFromKeycloak(Authentication authentication) {
        try {
            var keycloakId = (String) ((Jwt)authentication.getPrincipal()).getClaims().get("sub");

            Optional<UserEntity> existingUser = userRepository.findByKeycloakId(keycloakId);
            if (existingUser.isPresent()) {
                return existingUser.get();
            }

            Optional<org.keycloak.representations.idm.UserRepresentation> keycloakUser = 
                    keycloakService.getUserById(keycloakId);
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

