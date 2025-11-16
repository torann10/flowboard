package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.enums.UserRole;

import java.util.List;
import java.util.UUID;

@Repository
public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    boolean existsByIdAndProjectUsersUserId(UUID projectId, UUID userId);
    List<ProjectEntity> findAllByProjectUsersUserIdAndProjectUsersRole(UUID userId, UserRole role);
}
