package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.TimeLogEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLogEntity, UUID> {
    List<TimeLogEntity> findByUserId(UUID userId);
    Optional<TimeLogEntity> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    List<TimeLogEntity> findAllByTaskProjectIdAndLogDateBetween(UUID projectId, LocalDate startDate, LocalDate endDate);
    List<TimeLogEntity> findByTaskProjectIdInAndLogDateBetween(Set<UUID> projectId, LocalDate startDate, LocalDate endDate);
}
