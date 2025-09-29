package szte.flowboard.service;

import org.springframework.stereotype.Service;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.repository.ReportRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ReportService {

    private final ReportRepository reportRepository;

    public ReportService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public ReportEntity create(ReportEntity report) {
        return reportRepository.save(report);
    }

    public List<ReportEntity> findAll() {
        return reportRepository.findAll();
    }

    public Optional<ReportEntity> findById(UUID id) {
        return reportRepository.findById(id);
    }

    public ReportEntity update(ReportEntity report) {
        return reportRepository.save(report);
    }

    public void delete(UUID id) {
        reportRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return reportRepository.existsById(id);
    }

    public long count() {
        return reportRepository.count();
    }
}
