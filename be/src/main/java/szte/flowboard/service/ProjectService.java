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

    public boolean existsByIdAndUser(UUID id, Authentication authentication) {
        var user = userService.getUserByAuthentication(authentication);

        return user
                .filter(userEntity -> projectUserRepository.existsByUserIdAndProjectId(userEntity.getId(), id))
                .isPresent();

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
        reportRepository.deleteByProjectId(id);
        // Delete project-user relationships first
        projectUserRepository.deleteByProjectId(id);
        // Then delete the project
        projectRepository.deleteById(id);
    }
}
