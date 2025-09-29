package szte.flowboard.service;

import org.springframework.stereotype.Service;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.repository.TaskRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public TaskEntity create(TaskEntity task) {
        return taskRepository.save(task);
    }

    public List<TaskEntity> findAll() {
        return taskRepository.findAll();
    }

    public Optional<TaskEntity> findById(UUID id) {
        return taskRepository.findById(id);
    }

    public TaskEntity update(TaskEntity task) {
        return taskRepository.save(task);
    }

    public void delete(UUID id) {
        taskRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return taskRepository.existsById(id);
    }

    public long count() {
        return taskRepository.count();
    }
}
