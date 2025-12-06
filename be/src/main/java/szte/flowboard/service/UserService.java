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

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public Optional<UserEntity> getUserByAuthentication(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);

        return userRepository.findByKeycloakId(keycloakId);
    }

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

    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}