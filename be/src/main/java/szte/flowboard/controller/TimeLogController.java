package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.dto.TimeLogDto;
import szte.flowboard.dto.request.TimeLogUpdateRequestDto;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.mapper.TimeLogMapper;
import szte.flowboard.service.TimeLogService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing time log entries.
 * Provides endpoints for creating, reading, updating, and deleting time logs.
 * All operations are scoped to the authenticated user's accessible time logs.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/time-logs")
public class TimeLogController {

    private final TimeLogService timeLogService;
    private final TimeLogMapper timeLogMapper;

    /**
     * Creates a new time log entry for a task that the current user has access to.
     *
     * @param timeLogRequest the time log creation request containing time log details
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the created time log DTO with HTTP status 201, or 400 if input is invalid
     */
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

    /**
     * Retrieves all time log entries accessible by the current user.
     *
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing a list of time log DTOs with HTTP status 200
     */
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

    /**
     * Retrieves all time log entries for a specific task if the current user has access to it.
     *
     * @param taskId the unique identifier of the task
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing a list of time log DTOs with HTTP status 200
     */
    @Operation(operationId = "getAllTimeLogsByTask", summary = "Get all time logs for a task", description = "Retrieves all time log entries for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time logs retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TimeLogDto.class))))
    })
    @GetMapping("task/{taskId}")
    public ResponseEntity<List<TimeLogDto>> findAllByTask(@PathVariable UUID taskId, Authentication authentication) {
        List<TimeLogEntity> timeLogs = timeLogService.findAllByTaskId(taskId, authentication);
        List<TimeLogDto> timeLogDtos = timeLogMapper.toDtoList(timeLogs);
        return ResponseEntity.ok(timeLogDtos);
    }

    /**
     * Retrieves a time log by its ID if the current user has access to it.
     *
     * @param id the unique identifier of the time log
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the time log DTO with HTTP status 200 if found, or 404 if not found or no access
     */
    @Operation(operationId = "getTimeLogById", summary = "Get time log by ID", description = "Retrieves a time log by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Time log found successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TimeLogDto.class)))),
            @ApiResponse(responseCode = "404", description = "Time log not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TimeLogDto> findById(@PathVariable UUID id, Authentication authentication) {
        Optional<TimeLogEntity> timeLog = timeLogService.findByIdAndUser(id, authentication);
        return timeLog.map(tl -> ResponseEntity.ok(timeLogMapper.toDto(tl)))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing time log if the current user has access to it.
     *
     * @param id the unique identifier of the time log to update
     * @param timeLogRequest the time log update request containing updated time log details
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the updated time log DTO with HTTP status 200, 404 if not found, or 400 if input is invalid
     */
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

    /**
     * Deletes a time log by its ID if the current user has access to it.
     *
     * @param id the unique identifier of the time log to delete
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity with HTTP status 204 if deleted successfully, or 404 if not found or no access
     */
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
}
