package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.ProjectRepository;
import szte.flowboard.repository.ProjectUserRepository;
import szte.flowboard.repository.TaskRepository;
import szte.flowboard.repository.TimeLogRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final UserService userService;
    private final ProjectUserRepository projectUserRepository;
    private final TaskRepository taskRepository;

    public TimeLogService(TimeLogRepository timeLogRepository, UserService userService, ProjectUserRepository projectUserRepository, TaskRepository taskRepository) {
        this.timeLogRepository = timeLogRepository;
        this.userService = userService;
        this.projectUserRepository = projectUserRepository;
        this.taskRepository = taskRepository;
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

    public List<TimeLogEntity> findAllByTaskId(UUID taskId, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return List.of();
        }

        var task = taskRepository.findById(taskId);

        if (task.isEmpty()) {
            return List.of();
        }

        var userExists = projectUserRepository.existsByUserIdAndProjectId(user.get().getId(), task.get().getProject().getId());

        if (!userExists) {
            return List.of();
        }

        return task.get().getTimeLogs();
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
