package szte.flowboard.service;

import org.springframework.stereotype.Service;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.repository.TimeLogRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;

    public TimeLogService(TimeLogRepository timeLogRepository) {
        this.timeLogRepository = timeLogRepository;
    }

    public TimeLogEntity create(TimeLogEntity timeLog) {
        return timeLogRepository.save(timeLog);
    }

    public List<TimeLogEntity> findAll() {
        return timeLogRepository.findAll();
    }

    public Optional<TimeLogEntity> findById(UUID id) {
        return timeLogRepository.findById(id);
    }

    public TimeLogEntity update(TimeLogEntity timeLog) {
        return timeLogRepository.save(timeLog);
    }

    public void delete(UUID id) {
        timeLogRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return timeLogRepository.existsById(id);
    }

    public long count() {
        return timeLogRepository.count();
    }
}
