package szte.flowboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.service.TimeLogService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/time-logs")
public class TimeLogController {

    private final TimeLogService timeLogService;

    @PostMapping
    public ResponseEntity<TimeLogEntity> create(@RequestBody TimeLogEntity timeLog) {
        TimeLogEntity createdTimeLog = timeLogService.create(timeLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTimeLog);
    }

    @GetMapping
    public ResponseEntity<List<TimeLogEntity>> findAll() {
        List<TimeLogEntity> timeLogs = timeLogService.findAll();
        return ResponseEntity.ok(timeLogs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TimeLogEntity> findById(@PathVariable UUID id) {
        Optional<TimeLogEntity> timeLog = timeLogService.findById(id);
        return timeLog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TimeLogEntity> update(@PathVariable UUID id, @RequestBody TimeLogEntity timeLog) {
        if (!timeLogService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timeLog.setId(id);
        TimeLogEntity updatedTimeLog = timeLogService.update(timeLog);
        return ResponseEntity.ok(updatedTimeLog);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!timeLogService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timeLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = timeLogService.count();
        return ResponseEntity.ok(count);
    }
}
