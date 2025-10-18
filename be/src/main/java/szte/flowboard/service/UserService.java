package szte.flowboard.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import szte.flowboard.dto.request.UserCreateRequest;
import szte.flowboard.dto.request.UserUpdateRequest;
import szte.flowboard.dto.response.UserResponse;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    public UserEntity create(UserEntity user) {
        return userRepository.save(user);
    }

    public UserResponse create(UserCreateRequest userCreateRequest) {
        UserEntity userEntity = new UserEntity();
        userEntity.setKeycloakId(userCreateRequest.keycloakId());
        userEntity.setFirstName(userCreateRequest.firstName());
        userEntity.setLastName(userCreateRequest.lastName());
        userEntity.setEmailAddress(userCreateRequest.emailAddress());
        
        UserEntity savedUser = userRepository.save(userEntity);
        
        return new UserResponse(
                savedUser.getId(),
                savedUser.getKeycloakId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmailAddress()
        );
    }

    public UserEntity update(UserEntity user) {
        return userRepository.save(user);
    }

    public UserResponse update(UUID id, UserUpdateRequest userUpdateRequest) {
        UserEntity existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
        
        existingUser.setFirstName(userUpdateRequest.firstName());
        existingUser.setLastName(userUpdateRequest.lastName());
        existingUser.setEmailAddress(userUpdateRequest.emailAddress());
        
        UserEntity savedUser = userRepository.save(existingUser);
        
        return new UserResponse(
                savedUser.getId(),
                savedUser.getKeycloakId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmailAddress()
        );
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

    public Optional<UserResponse> findById(UUID id) {
        return userRepository.findById(id)
                .map(user -> new UserResponse(
                        user.getId(),
                        user.getKeycloakId(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.getEmailAddress()
                ));
    }

    public Optional<UserEntity> findByKeycloakId(String keycloakId) {
        return userRepository.findByKeycloakId(keycloakId);
    }


    @Transactional
    public void delete(UUID id) {
        UserEntity user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));

        try {
            keycloakService.deleteUser(user.getKeycloakId());

            userRepository.deleteById(id);
            log.info("User deleted successfully: {}", id);
        } catch (Exception e) {
            log.error("Error deleting user: {}", id, e);
            throw new RuntimeException("Failed to delete user: " + e.getMessage());
        }
    }

    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    public long count() {
        return userRepository.count();
    }

    public Optional<UserEntity> findByEmailAddress(String email) {
        return userRepository.findByEmailAddress(email);
    }
}