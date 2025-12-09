package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.ProjectRepository;
import szte.flowboard.repository.TaskRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing tasks.
 * Handles task creation, retrieval, updates, and deletion with project access control.
 * All operations verify that the user has access to the project containing the task.
 */
@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserService userService;

    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository, UserService userService) {
        this.taskRepository = taskRepository;
        this.projectRepository = projectRepository;
        this.userService = userService;
    }

    /**
     * Creates a new task for a project that the current user has access to.
     *
     * @param task the task entity to create
     * @param authentication the authentication object containing the current user's information
     * @return the created task entity, or null if user not found or doesn't have access to the project
     */
    public TaskEntity create(TaskEntity task, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return null;
        }

        var userHasAccess = projectRepository.existsByIdAndProjectUsersUserId(task.getProject().getId(), user.get().getId());

        if (!userHasAccess) {
            return null;
        }

        return taskRepository.save(task);
    }

    /**
     * Retrieves all tasks accessible by the current user (tasks from projects the user has access to).
     *
     * @param authentication the authentication object containing the current user's information
     * @return a list of task entities accessible by the user, or an empty list if user not found
     */
    public List<TaskEntity> findAllByUser(Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        // Get tasks assigned to the user and unassigned tasks
        return taskRepository.findByProjectProjectUsersUserId(user.get().getId());
    }

    /**
     * Finds a task by ID if the current user has access to the project containing the task.
     *
     * @param id the unique identifier of the task
     * @param authentication the authentication object containing the current user's information
     * @return an Optional containing the task if found and accessible, empty otherwise
     */
    public Optional<TaskEntity> findByIdAndUser(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        return taskRepository.findByIdAndProjectProjectUsersUserId(id, user.get().getId());
    }

    /**
     * Checks if a task exists and the current user has access to the project containing the task.
     *
     * @param id the unique identifier of the task
     * @param authentication the authentication object containing the current user's information
     * @return true if the task exists and the user has access, false otherwise
     */
    public boolean existsById(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);

        return user.filter(userEntity -> taskRepository.existsByIdAndProjectProjectUsersUserId(id, userEntity.getId())).isPresent();
    }

    /**
     * Updates an existing task, preserving the project association.
     *
     * @param task the task entity with updated information
     * @return the updated task entity, or null if the task doesn't exist
     */
    public TaskEntity update(TaskEntity task) {
        var existingTask = taskRepository.findById(task.getId());

        if (existingTask.isEmpty()) {
            return null;
        }

        task.setProject(existingTask.get().getProject());

        return taskRepository.save(task);
    }

    /**
     * Deletes a task by its ID.
     *
     * @param id the unique identifier of the task to delete
     */
    public void delete(UUID id) {
        taskRepository.deleteById(id);
    }

    /**
     * Retrieves all tasks for a specific project.
     *
     * @param projectId the unique identifier of the project
     * @return a list of task entities for the project
     */
    public List<TaskEntity> findAllByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }
}
