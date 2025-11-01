package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import szte.flowboard.entity.BaseEntity;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.ProjectRepository;
import szte.flowboard.repository.ProjectUserRepository;
import szte.flowboard.repository.StoryPointTimeMappingRepository;
import szte.flowboard.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserRepository userRepository;
    private final StoryPointTimeMappingRepository storyPointTimeMappingRepository;

    public ProjectService(ProjectRepository projectRepository,
                          ProjectUserRepository projectUserRepository,
                          UserRepository userRepository,
                          StoryPointTimeMappingRepository storyPointTimeMappingRepository) {
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
        this.userRepository = userRepository;
        this.storyPointTimeMappingRepository = storyPointTimeMappingRepository;
    }

    public ProjectEntity create(ProjectEntity project, Authentication authentication) {
        // Get current user
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        // Save project
        ProjectEntity savedProject = projectRepository.save(project);
        
        // Create project-user relationship with ADMIN role
        ProjectUserEntity projectUser = new ProjectUserEntity();
        projectUser.setUser(user.get());
        projectUser.setProject(savedProject);
        projectUser.setRole(szte.flowboard.enums.UserRole.MAINTAINER);
        projectUserRepository.save(projectUser);
        
        return savedProject;
    }

    public List<ProjectEntity> findAllByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        List<ProjectUserEntity> projectUsers = projectUserRepository.findByUserId(user.get().getId());

        return projectUsers.stream()
                .map(ProjectUserEntity::getProject)
                .toList();
    }

    public Optional<ProjectEntity> findByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return Optional.empty();
        }
        
        // Check if user has access to this project
        Optional<ProjectUserEntity> projectUser = projectUserRepository.findByUserIdAndProjectId(user.get().getId(), id);
        if (projectUser.isEmpty()) {
            return Optional.empty();
        }
        
        return projectRepository.findById(id);
    }

    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return false;
        }
        
        return projectUserRepository.existsByUserIdAndProjectId(user.get().getId(), id);
    }

    public long countByUser(Authentication authentication) {
        String keycloakId = getKeycloakIdFromAuthentication(authentication);
        Optional<UserEntity> user = userRepository.findByKeycloakId(keycloakId);
        
        if (user.isEmpty()) {
            return 0;
        }
        
        return projectUserRepository.countByUserId(user.get().getId());
    }

    @Transactional
    public ProjectEntity update(ProjectEntity project) {
        UUID[] ids = project.getStoryPointTimeMappings().stream()
                .map(BaseEntity::getId)
                .filter(Objects::nonNull)
                .toArray(UUID[]::new);

        this.storyPointTimeMappingRepository.deleteAllForProjectNotInIds(project.getId(), ids);

        return projectRepository.save(project);
    }

    @Transactional
    public void delete(UUID id) {
        // Delete project-user relationships first
        projectUserRepository.deleteByProjectId(id);
        // Then delete the project
        projectRepository.deleteById(id);
    }

    private String getKeycloakIdFromAuthentication(Authentication authentication) {
        return (String) ((Jwt) authentication.getPrincipal()).getClaims().get("sub");
    }
}
