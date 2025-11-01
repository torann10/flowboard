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

@Service
public class ProjectUserService {

    private final UserService userService;
    private final ProjectUserRepository projectUserRepository;

    public ProjectUserService(UserService userService, ProjectUserRepository projectUserRepository) {
        this.userService = userService;
        this.projectUserRepository = projectUserRepository;
    }

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

    public List<ProjectUserEntity> findAll() {
        return projectUserRepository.findAll();
    }

    public Optional<ProjectUserEntity> findById(UUID id) {
        return projectUserRepository.findById(id);
    }

    public ProjectUserEntity update(UUID id, UserRole role) {
        var optionalProjectUser = projectUserRepository.findById(id);

        if (optionalProjectUser.isEmpty()) {
            return null;
        }

        var projectUser = optionalProjectUser.get();

        if (projectUser.getRole() != UserRole.MAINTAINER && projectUser.getRole() != UserRole.EDITOR) {
            return null;
        }

        projectUser.setRole(role);

        return projectUserRepository.save(projectUser);
    }

    public void delete(UUID id) {
        projectUserRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return projectUserRepository.existsById(id);
    }

    public long count() {
        return projectUserRepository.count();
    }
}
