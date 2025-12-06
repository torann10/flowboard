package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.ReportEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<ReportEntity, UUID> {
    List<ReportEntity> findByUserId(UUID userId);
    Optional<ReportEntity> findByIdAndUserId(UUID id, UUID userId);
    Integer deleteByIdAndUserId(UUID id, UUID userId);
    @Modifying
    @Query("update ReportEntity r set r.name = ?1 where r.id = ?2 AND r.user.id = ?3")
    Integer renameReportByIdAndUserId(String name, UUID id, UUID userId);
    void deleteByProjectId(UUID projectId);
}
