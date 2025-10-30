package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.TimeLogEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLogEntity, UUID> {
    List<TimeLogEntity> findByUserId(UUID userId);
    Optional<TimeLogEntity> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
