package szte.flowboard.service;

import org.springframework.stereotype.Service;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.repository.ProjectRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ProjectService {

    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public ProjectEntity create(ProjectEntity project) {
        return projectRepository.save(project);
    }

    public List<ProjectEntity> findAll() {
        return projectRepository.findAll();
    }

    public Optional<ProjectEntity> findById(UUID id) {
        return projectRepository.findById(id);
    }

    public ProjectEntity update(ProjectEntity project) {
        return projectRepository.save(project);
    }

    public void delete(UUID id) {
        projectRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return projectRepository.existsById(id);
    }

    public long count() {
        return projectRepository.count();
    }
}
