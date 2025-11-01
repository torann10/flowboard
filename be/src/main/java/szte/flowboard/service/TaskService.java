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

    public List<TaskEntity> findAllByUser(Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        // Get tasks assigned to the user and unassigned tasks
        return taskRepository.findByProjectProjectUsersUserId(user.get().getId());
    }

    public Optional<TaskEntity> findByIdAndUser(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        return taskRepository.findByIdAndProjectProjectUsersUserId(id, user.get().getId());
    }

    public boolean existsById(UUID id, Authentication authentication) {
        Optional<UserEntity> user = userService.getUserByAuthentication(authentication);

        return user.filter(userEntity -> taskRepository.existsByIdAndProjectProjectUsersUserId(id, userEntity.getId())).isPresent();
    }

    public TaskEntity update(TaskEntity task) {
        var existingTask = taskRepository.findById(task.getId());

        if (existingTask.isEmpty()) {
            return null;
        }

        task.setProject(existingTask.get().getProject());

        return taskRepository.save(task);
    }

    public void delete(UUID id) {
        taskRepository.deleteById(id);
    }

    public List<TaskEntity> findAllByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }
}
