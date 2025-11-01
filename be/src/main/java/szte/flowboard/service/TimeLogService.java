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
    private final UserService userService;

    public TimeLogService(TimeLogRepository timeLogRepository, UserService userService) {
        this.timeLogRepository = timeLogRepository;
        this.userService = userService;
    }

    public TimeLogEntity create(TimeLogEntity timeLog, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return null;
        }
        
        timeLog.setUser(user.get());
        
        return timeLogRepository.save(timeLog);
    }

    public List<TimeLogEntity> findAllByUser(Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        return timeLogRepository.findByUserId(user.get().getId());
    }

    public Optional<TimeLogEntity> findByIdAndUser(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        return timeLogRepository.findByIdAndUserId(id, user.get().getId());
    }

    public TimeLogEntity update(TimeLogEntity timeLog, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        timeLog.setUser(user.get());
        
        return timeLogRepository.save(timeLog);
    }

    public void delete(UUID id) {
        timeLogRepository.deleteById(id);
    }

    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);

        return user.filter(userEntity -> timeLogRepository.existsByIdAndUserId(id, userEntity.getId())).isPresent();

    }

    public long countByUser(Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return 0;
        }
        
        return timeLogRepository.countByUserId(user.get().getId());
    }

    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}
