package szte.flowboard.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import szte.flowboard.entity.TimeLogEntity;
import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLogEntity, UUID> {
}
