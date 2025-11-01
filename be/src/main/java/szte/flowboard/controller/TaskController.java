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
import szte.flowboard.dto.TaskDto;
import szte.flowboard.dto.TaskCreateRequestDto;
import szte.flowboard.dto.TaskUpdateRequestDto;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.mapper.TaskMapper;
import szte.flowboard.service.TaskService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;
    private final TaskMapper taskMapper;

    @Operation(operationId = "createTask", summary = "Create task", description = "Creates a new task for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<TaskDto> create(@Valid @RequestBody TaskCreateRequestDto taskRequest, Authentication authentication) {
        TaskEntity task = taskMapper.toEntity(taskRequest);
        TaskEntity createdTask = taskService.create(task, authentication);
        TaskDto taskDto = taskMapper.toDto(createdTask);
        return ResponseEntity.status(HttpStatus.CREATED).body(taskDto);
    }

    @Operation(operationId = "getAllTasks", summary = "Get all tasks", description = "Retrieves all tasks for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<TaskDto>> findAll(Authentication authentication) {
        List<TaskEntity> tasks = taskService.findAllByUser(authentication);
        List<TaskDto> taskDtos = taskMapper.toDtoList(tasks);
        return ResponseEntity.ok(taskDtos);
    }

    @Operation(operationId = "getTasksByProject", summary = "Get tasks by project", description = "Retrieves all tasks for a specific project")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = TaskDto.class))))
    })
    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskDto>> findByProject(@PathVariable UUID projectId) {
        List<TaskEntity> tasks = taskService.findAllByProject(projectId);
        List<TaskDto> taskDtos = taskMapper.toDtoList(tasks);
        return ResponseEntity.ok(taskDtos);
    }

    @Operation(operationId = "getTaskById", summary = "Get task by ID", description = "Retrieves a task by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<TaskDto> findById(@PathVariable UUID id, Authentication authentication) {
        Optional<TaskEntity> task = taskService.findByIdAndUser(id, authentication);
        return task.map(t -> ResponseEntity.ok(taskMapper.toDto(t)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(operationId = "updateTask", summary = "Update task", description = "Updates an existing task for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = TaskDto.class))),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<TaskDto> update(@PathVariable UUID id, @Valid @RequestBody TaskUpdateRequestDto taskRequest, Authentication authentication) {
        if (!taskService.existsById(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        TaskEntity task = taskMapper.toEntity(taskRequest);
        task.setId(id);
        TaskEntity updatedTask = taskService.update(task);
        TaskDto taskDto = taskMapper.toDto(updatedTask);
        return ResponseEntity.ok(taskDto);
    }

    @Operation(operationId = "deleteTask", summary = "Delete task", description = "Deletes a task by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        if (!taskService.existsById(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
