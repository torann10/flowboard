package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.TimeLogRepository;
import szte.flowboard.repository.UserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final UserRepository userRepository;

    public TimeLogService(TimeLogRepository timeLogRepository, UserRepository userRepository) {
        this.timeLogRepository = timeLogRepository;
        this.userRepository = userRepository;
    }

    public TimeLogEntity create(TimeLogEntity timeLog, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        timeLog.setUserId(user.get().getId());
        
        return timeLogRepository.save(timeLog);
    }

    public List<TimeLogEntity> findAllByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        return timeLogRepository.findByUserId(user.get().getId());
    }

    public Optional<TimeLogEntity> findByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        return timeLogRepository.findByIdAndUserId(id, user.get().getId());
    }

    public TimeLogEntity update(TimeLogEntity timeLog, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        timeLog.setUserId(user.get().getId());
        
        return timeLogRepository.save(timeLog);
    }

    public void delete(UUID id) {
        timeLogRepository.deleteById(id);
    }

    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return false;
        }
        
        return timeLogRepository.existsByIdAndUserId(id, user.get().getId());
    }

    public long countByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return 0;
        }
        
        return timeLogRepository.countByUserId(user.get().getId());
    }

    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}
