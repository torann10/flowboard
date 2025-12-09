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

/**
 * Service for managing time log entries.
 * Handles time log creation, retrieval, updates, and deletion with user and project access control.
 * Time logs track the time spent by users on specific tasks.
 */
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

    /**
     * Creates a new time log entry and associates it with the current user.
     *
     * @param timeLog the time log entity to create
     * @param authentication the authentication object containing the current user's information
     * @return the created time log entity, or null if user not found
     */
    public TimeLogEntity create(TimeLogEntity timeLog, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return null;
        }
        
        timeLog.setUser(user.get());
        
        return timeLogRepository.save(timeLog);
    }

    /**
     * Retrieves all time log entries for the current user.
     *
     * @param authentication the authentication object containing the current user's information
     * @return a list of time log entities for the user, or an empty list if user not found
     */
    public List<TimeLogEntity> findAllByUser(Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        return timeLogRepository.findByUserId(user.get().getId());
    }

    /**
     * Finds a time log by ID if it belongs to the current user.
     *
     * @param id the unique identifier of the time log
     * @param authentication the authentication object containing the current user's information
     * @return an Optional containing the time log if found and belongs to the user, empty otherwise
     */
    public Optional<TimeLogEntity> findByIdAndUser(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        return timeLogRepository.findByIdAndUserId(id, user.get().getId());
    }

    /**
     * Retrieves all time log entries for a specific task if the current user has access to the project.
     *
     * @param taskId the unique identifier of the task
     * @param authentication the authentication object containing the current user's information
     * @return a list of time log entities for the task, or an empty list if user not found or no access
     */
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

    /**
     * Updates an existing time log entry and associates it with the current user.
     *
     * @param timeLog the time log entity with updated information
     * @param authentication the authentication object containing the current user's information
     * @return the updated time log entity
     * @throws RuntimeException if the user is not found
     */
    public TimeLogEntity update(TimeLogEntity timeLog, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        timeLog.setUser(user.get());
        
        return timeLogRepository.save(timeLog);
    }

    /**
     * Deletes a time log entry by its ID.
     *
     * @param id the unique identifier of the time log to delete
     */
    public void delete(UUID id) {
        timeLogRepository.deleteById(id);
    }

    /**
     * Checks if a time log exists and belongs to the current user.
     *
     * @param id the unique identifier of the time log
     * @param authentication the authentication object containing the current user's information
     * @return true if the time log exists and belongs to the user, false otherwise
     */
    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);

        return user.filter(userEntity -> timeLogRepository.existsByIdAndUserId(id, userEntity.getId())).isPresent();
    }
}
