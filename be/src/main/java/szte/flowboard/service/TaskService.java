package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.TaskRepository;
import szte.flowboard.repository.UserRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public TaskService(TaskRepository taskRepository, UserRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public TaskEntity create(TaskEntity task, Authentication authentication) {
        // Validate that assignTo user exists if provided
        if (task.getAssignTo() != null) {
            Optional<UserEntity> assignedUser = userRepository.findById(task.getAssignTo());
            if (assignedUser.isEmpty()) {
                throw new RuntimeException("Assigned user not found");
            }
        }
        
        return taskRepository.save(task);
    }

    public List<TaskEntity> findAllByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        // Get tasks assigned to the user and unassigned tasks
        List<TaskEntity> assignedTasks = taskRepository.findByAssignTo(user.get().getId());
        List<TaskEntity> unassignedTasks = taskRepository.findByAssignToIsNull();
        
        // Combine both lists
        List<TaskEntity> allTasks = new ArrayList<>();
        allTasks.addAll(assignedTasks);
        allTasks.addAll(unassignedTasks);
        
        return allTasks;
    }

    public Optional<TaskEntity> findByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        // Check if task is assigned to the user
        Optional<TaskEntity> assignedTask = taskRepository.findByIdAndAssignTo(id, user.get().getId());
        if (assignedTask.isPresent()) {
            return assignedTask;
        }
        
        // Check if task is unassigned
        Optional<TaskEntity> task = taskRepository.findById(id);
        if (task.isPresent() && task.get().getAssignTo() == null) {
            return task;
        }
        
        return Optional.empty();
    }

    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return false;
        }
        
        // Check if task is assigned to the user
        if (taskRepository.existsByIdAndAssignTo(id, user.get().getId())) {
            return true;
        }
        
        // Check if task is unassigned
        Optional<TaskEntity> task = taskRepository.findById(id);
        return task.isPresent() && task.get().getAssignTo() == null;
    }

    public long countByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return 0;
        }
        
        long assignedCount = taskRepository.countByAssignTo(user.get().getId());
        long unassignedCount = taskRepository.findByAssignToIsNull().size();
        
        return assignedCount + unassignedCount;
    }

    public TaskEntity update(TaskEntity task) {
        return taskRepository.save(task);
    }

    public void delete(UUID id) {
        taskRepository.deleteById(id);
    }

    public List<TaskEntity> findAllByProject(UUID projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}
