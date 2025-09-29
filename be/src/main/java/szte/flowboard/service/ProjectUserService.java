package szte.flowboard.service;

import org.springframework.stereotype.Service;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.repository.ProjectUserRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectUserService {

    private final ProjectUserRepository projectUserRepository;

    public ProjectUserService(ProjectUserRepository projectUserRepository) {
        this.projectUserRepository = projectUserRepository;
    }

    public ProjectUserEntity create(ProjectUserEntity projectUser) {
        return projectUserRepository.save(projectUser);
    }

    public List<ProjectUserEntity> findAll() {
        return projectUserRepository.findAll();
    }

    public Optional<ProjectUserEntity> findById(UUID id) {
        return projectUserRepository.findById(id);
    }

    public ProjectUserEntity update(ProjectUserEntity projectUser) {
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
