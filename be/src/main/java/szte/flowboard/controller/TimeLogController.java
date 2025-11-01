package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.dto.TimeLogDto;
import szte.flowboard.dto.TimeLogUpdateRequestDto;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.mapper.TimeLogMapper;
import szte.flowboard.service.TimeLogService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/time-logs")
public class TimeLogController {

    private final TimeLogService timeLogService;
    private final TimeLogMapper timeLogMapper;

    @Operation(operationId = "createTimeLog", summary = "Create time log", description = "Creates a new time log entry for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Time log created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeLogDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<TimeLogDto> create(@Valid @RequestBody TimeLogUpdateRequestDto timeLogRequest, Authentication authentication) {
        TimeLogEntity timeLog = timeLogMapper.toEntity(timeLogRequest);
        TimeLogEntity createdTimeLog = timeLogService.create(timeLog, authentication);
        TimeLogDto timeLogDto = timeLogMapper.toDto(createdTimeLog);
        return ResponseEntity.status(HttpStatus.CREATED).body(timeLogDto);
    }

    @Operation(operationId = "getAllTimeLogs", summary = "Get all time logs", description = "Retrieves all time log entries for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time logs retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TimeLogDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<TimeLogDto>> findAll(Authentication authentication) {
        List<TimeLogEntity> timeLogs = timeLogService.findAllByUser(authentication);
        List<TimeLogDto> timeLogDtos = timeLogMapper.toDtoList(timeLogs);
        return ResponseEntity.ok(timeLogDtos);
    }

    @Operation(operationId = "getTimeLogById", summary = "Get time log by ID", description = "Retrieves a time log by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time log found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeLogDto.class))),
            @ApiResponse(responseCode = "404", description = "Time log not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TimeLogDto> findById(@PathVariable UUID id, Authentication authentication) {
        Optional<TimeLogEntity> timeLog = timeLogService.findByIdAndUser(id, authentication);
        return timeLog.map(tl -> ResponseEntity.ok(timeLogMapper.toDto(tl)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(operationId = "updateTimeLog", summary = "Update time log", description = "Updates an existing time log for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time log updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TimeLogDto.class))),
            @ApiResponse(responseCode = "404", description = "Time log not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TimeLogDto> update(@PathVariable UUID id, @Valid @RequestBody TimeLogUpdateRequestDto timeLogRequest, Authentication authentication) {
        if (!timeLogService.existsByIdAndUser(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        TimeLogEntity timeLog = timeLogMapper.toEntity(timeLogRequest);
        timeLog.setId(id);
        TimeLogEntity updatedTimeLog = timeLogService.update(timeLog, authentication);
        TimeLogDto timeLogDto = timeLogMapper.toDto(updatedTimeLog);
        return ResponseEntity.ok(timeLogDto);
    }

    @Operation(operationId = "deleteTimeLog", summary = "Delete time log", description = "Deletes a time log by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Time log deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Time log not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        if (!timeLogService.existsByIdAndUser(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        timeLogService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(operationId = "getTimeLogCount", summary = "Get time log count", description = "Retrieves the total number of time logs for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Long> count(Authentication authentication) {
        long count = timeLogService.countByUser(authentication);
        return ResponseEntity.ok(count);
    }
}
