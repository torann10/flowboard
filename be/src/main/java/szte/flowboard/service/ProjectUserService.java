package szte.flowboard.service;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.enums.UserRole;
import szte.flowboard.repository.ProjectRepository;
import szte.flowboard.repository.ProjectUserRepository;
import szte.flowboard.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing project-user relationships.
 * Handles the association between users and projects, including roles and fees.
 * Only MAINTAINER role users can create or update project-user relationships.
 */
@Service
public class ProjectUserService {

    private final UserService userService;
    private final ProjectUserRepository projectUserRepository;

    public ProjectUserService(UserService userService, ProjectUserRepository projectUserRepository) {
        this.userService = userService;
        this.projectUserRepository = projectUserRepository;
    }

    /**
     * Creates a new project-user relationship.
     * Only users with MAINTAINER role on the project can create relationships.
     *
     * @param projectUser the project-user entity to create
     * @param authentication the authentication object containing the current user's information
     * @return the created project-user entity, or null if user not found or doesn't have MAINTAINER role
     */
    public ProjectUserEntity create(ProjectUserEntity projectUser, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        if (user.isEmpty()) {
            return null;
        }

        if (!projectUserRepository.existsByUserIdAndProjectIdAndRole(user.get().getId(), projectUser.getProject().getId(), UserRole.MAINTAINER)) {
            return null;
        }

        return projectUserRepository.save(projectUser);
    }

    /**
     * Retrieves all project-user relationships in the system.
     *
     * @return a list of all project-user entities
     */
    public List<ProjectUserEntity> findAll() {
        return projectUserRepository.findAll();
    }

    /**
     * Finds a project-user relationship by its ID.
     *
     * @param id the unique identifier of the project-user relationship
     * @return an Optional containing the project-user entity if found, empty otherwise
     */
    public Optional<ProjectUserEntity> findById(UUID id) {
        return projectUserRepository.findById(id);
    }

    /**
     * Updates a project-user relationship's role and fee.
     * Only MAINTAINER role relationships can be updated.
     *
     * @param id the unique identifier of the project-user relationship to update
     * @param role the new role for the relationship
     * @param fee the new fee for the relationship
     * @return the updated project-user entity, or null if not found or not a MAINTAINER relationship
     */
    public ProjectUserEntity update(UUID id, UserRole role, Double fee) {
        var optionalProjectUser = projectUserRepository.findById(id);

        if (optionalProjectUser.isEmpty()) {
            return null;
        }

        var projectUser = optionalProjectUser.get();

        if (projectUser.getRole() != UserRole.MAINTAINER) {
            return null;
        }

        projectUser.setRole(role);
        projectUser.setFee(fee);

        return projectUserRepository.save(projectUser);
    }

    /**
     * Deletes a project-user relationship by its ID.
     *
     * @param id the unique identifier of the project-user relationship to delete
     */
    public void delete(UUID id) {
        projectUserRepository.deleteById(id);
    }

    /**
     * Checks if a project-user relationship exists by its ID.
     *
     * @param id the unique identifier of the project-user relationship
     * @return true if the relationship exists, false otherwise
     */
    public boolean existsById(UUID id) {
        return projectUserRepository.existsById(id);
    }
}
