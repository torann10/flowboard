package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

    @Operation(summary = "Create time log", description = "Creates a new time log entry")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Time log created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<TimeLogEntity> create(@RequestBody TimeLogEntity timeLog) {
        TimeLogEntity createdTimeLog = timeLogService.create(timeLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTimeLog);
    }

    @Operation(summary = "Get all time logs", description = "Retrieves all time log entries")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time logs retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<TimeLogEntity>> findAll() {
        List<TimeLogEntity> timeLogs = timeLogService.findAll();
        return ResponseEntity.ok(timeLogs);
    }

    @Operation(summary = "Get time log by ID", description = "Retrieves a time log by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time log found successfully"),
            @ApiResponse(responseCode = "404", description = "Time log not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TimeLogEntity> findById(@PathVariable UUID id) {
        Optional<TimeLogEntity> timeLog = timeLogService.findById(id);
        return timeLog.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update time log", description = "Updates an existing time log")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time log updated successfully"),
            @ApiResponse(responseCode = "404", description = "Time log not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TimeLogEntity> update(@PathVariable UUID id, @RequestBody TimeLogEntity timeLog) {
        if (!timeLogService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timeLog.setId(id);
        TimeLogEntity updatedTimeLog = timeLogService.update(timeLog);
        return ResponseEntity.ok(updatedTimeLog);
    }

    @Operation(summary = "Delete time log", description = "Deletes a time log by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Time log deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Time log not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!timeLogService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        timeLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get time log count", description = "Retrieves the total number of time logs")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = timeLogService.count();
        return ResponseEntity.ok(count);
    }
}
