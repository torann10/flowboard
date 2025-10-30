package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.TaskEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByAssignTo(UUID assignTo);
    List<TaskEntity> findByAssignToIsNull();
    List<TaskEntity> findByProjectId(UUID projectId);
    Optional<TaskEntity> findByIdAndAssignTo(UUID id, UUID assignTo);
    boolean existsByIdAndAssignTo(UUID id, UUID assignTo);
    long countByAssignTo(UUID assignTo);
}
