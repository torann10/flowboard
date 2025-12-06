package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.TaskEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<TaskEntity, UUID> {
    List<TaskEntity> findByProjectProjectUsersUserId(UUID userId);
    List<TaskEntity> findByProjectId(UUID projectId);
    Optional<TaskEntity> findByIdAndProjectProjectUsersUserId(UUID id, UUID userId);
    boolean existsByIdAndProjectProjectUsersUserId(UUID id, UUID userId);
    List<TaskEntity> findByProjectIdAndFinishedAtBetween(UUID projectId, LocalDateTime startDate, LocalDateTime endDate);
}
