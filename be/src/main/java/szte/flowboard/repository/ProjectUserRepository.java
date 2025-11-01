package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.enums.UserRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProjectUserRepository extends JpaRepository<ProjectUserEntity, UUID> {
    List<ProjectUserEntity> findByUserId(UUID userId);
    Optional<ProjectUserEntity> findByUserIdAndProjectId(UUID userId, UUID projectId);
    boolean existsByUserIdAndProjectId(UUID userId, UUID projectId);
    boolean existsByUserIdAndProjectIdAndRole(UUID userId, UUID projectId, UserRole role);
    long countByUserId(UUID userId);
    void deleteByProjectId(UUID projectId);
}
