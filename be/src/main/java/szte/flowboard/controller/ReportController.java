package szte.flowboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.service.ReportService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    public ResponseEntity<ReportEntity> create(@RequestBody ReportEntity report) {
        ReportEntity createdReport = reportService.create(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReport);
    }

    @GetMapping
    public ResponseEntity<List<ReportEntity>> findAll() {
        List<ReportEntity> reports = reportService.findAll();
        return ResponseEntity.ok(reports);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportEntity> findById(@PathVariable UUID id) {
        Optional<ReportEntity> report = reportService.findById(id);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ReportEntity> update(@PathVariable UUID id, @RequestBody ReportEntity report) {
        if (!reportService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        report.setId(id);
        ReportEntity updatedReport = reportService.update(report);
        return ResponseEntity.ok(updatedReport);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!reportService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = reportService.count();
        return ResponseEntity.ok(count);
    }
}
