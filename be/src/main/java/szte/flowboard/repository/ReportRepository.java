package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.ReportEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
    List<ReportEntity> findByUserId(UUID userId);
    Optional<ReportEntity> findByIdAndUserId(UUID id, UUID userId);
    boolean existsByIdAndUserId(UUID id, UUID userId);
    long countByUserId(UUID userId);
}
