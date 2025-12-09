package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import szte.flowboard.entity.BaseEntity;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.entity.UserEntity;
import szte.flowboard.repository.*;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing projects.
 * Handles project creation, retrieval, updates, and deletion with user access control.
 * Automatically assigns the creator as MAINTAINER role when creating a project.
 */
@Service
public class ProjectService {

    private final ReportRepository reportRepository;
    private final ProjectRepository projectRepository;
    private final ProjectUserRepository projectUserRepository;
    private final UserService userService;
    private final StoryPointTimeMappingRepository storyPointTimeMappingRepository;

    public ProjectService(ReportRepository reportRepository,
                          ProjectRepository projectRepository,
                          ProjectUserRepository projectUserRepository,
                          UserService userService,
                          StoryPointTimeMappingRepository storyPointTimeMappingRepository) {
        this.reportRepository = reportRepository;
        this.projectRepository = projectRepository;
        this.projectUserRepository = projectUserRepository;
        this.userService = userService;
        this.storyPointTimeMappingRepository = storyPointTimeMappingRepository;
    }

    /**
     * Creates a new project and assigns the current user as MAINTAINER.
     *
     * @param project the project entity to create
     * @param authentication the authentication object containing the current user's information
     * @return the created project entity
     * @throws RuntimeException if the user is not found
     */
    public ProjectEntity create(ProjectEntity project, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        ProjectEntity savedProject = projectRepository.save(project);
        
        ProjectUserEntity projectUser = new ProjectUserEntity();
        projectUser.setUser(user.get());
        projectUser.setProject(savedProject);
        projectUser.setRole(szte.flowboard.enums.UserRole.MAINTAINER);
        projectUserRepository.save(projectUser);
        
        return savedProject;
    }

    /**
     * Retrieves all projects accessible by the current user.
     *
     * @param authentication the authentication object containing the current user's information
     * @return a list of project entities accessible by the user, or an empty list if user not found
     */
    public List<ProjectEntity> findAllByUser(Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);
        
        if (user.isEmpty()) {
            return List.of();
        }
        
        List<ProjectUserEntity> projectUsers = projectUserRepository.findByUserId(user.get().getId());

        return projectUsers.stream()
                .map(ProjectUserEntity::getProject)
                .toList();
    }

    /**
     * Finds a project by ID if the current user has access to it.
     *
     * @param id the unique identifier of the project
     * @param authentication the authentication object containing the current user's information
     * @return an Optional containing the project if found and accessible, empty otherwise
     */
    public Optional<ProjectEntity> findByIdAndUser(UUID id, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);
        
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

    /**
     * Checks if a project exists and the current user has access to it.
     *
     * @param id the unique identifier of the project
     * @param authentication the authentication object containing the current user's information
     * @return true if the project exists and the user has access, false otherwise
     */
    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        return user
                .filter(userEntity -> projectUserRepository.existsByUserIdAndProjectId(userEntity.getId(), id))
                .isPresent();

    }

    /**
     * Updates an existing project and manages story point time mappings.
     * Removes any story point time mappings that are not in the updated project.
     *
     * @param project the project entity with updated information
     * @return the updated project entity
     */
    @Transactional
    public ProjectEntity update(ProjectEntity project) {
        UUID[] ids = project.getStoryPointTimeMappings().stream()
                .map(BaseEntity::getId)
                .filter(Objects::nonNull)
                .toArray(UUID[]::new);

        this.storyPointTimeMappingRepository.deleteAllForProjectNotInIds(project.getId(), ids);

        return projectRepository.save(project);
    }

    /**
     * Deletes a project and all associated data (reports, project-user relationships).
     * This operation is transactional and will cascade delete related entities.
     *
     * @param id the unique identifier of the project to delete
     */
    @Transactional
    public void delete(UUID id) {
        reportRepository.deleteByProjectId(id);
        // Delete project-user relationships first
        projectUserRepository.deleteByProjectId(id);
        // Then delete the project
        projectRepository.deleteById(id);
    }
}
