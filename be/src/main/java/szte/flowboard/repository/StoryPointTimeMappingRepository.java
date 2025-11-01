package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.StoryPointTimeMappingEntity;

import java.util.UUID;

@Repository
public interface StoryPointTimeMappingRepository extends JpaRepository<StoryPointTimeMappingEntity, UUID> {
    @Modifying
    @Query("DELETE FROM StoryPointTimeMappingEntity sptm WHERE sptm.project.id = :projectId AND sptm.id NOT IN :ids")
    void deleteAllForProjectNotInIds(UUID projectId, UUID[] ids);
}
