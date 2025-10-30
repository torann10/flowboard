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
import szte.flowboard.dto.ProjectDto;
import szte.flowboard.dto.ProjectCreateRequestDto;
import szte.flowboard.dto.ProjectUpdateRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.mapper.ProjectMapper;
import szte.flowboard.service.ProjectService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final ProjectMapper projectMapper;

    @Operation(operationId = "createProject", summary = "Create project", description = "Creates a new project and assigns the current user as admin")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ProjectDto> create(@Valid @RequestBody ProjectCreateRequestDto projectRequest, Authentication authentication) {
        ProjectEntity project = projectMapper.toEntity(projectRequest);
        ProjectEntity createdProject = projectService.create(project, authentication);
        ProjectDto projectDto = projectMapper.toDto(createdProject);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectDto);
    }

    @Operation(operationId = "getAllProjects", summary = "Get all projects", description = "Retrieves all projects for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Projects retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<ProjectDto>> findAll(Authentication authentication) {
        List<ProjectEntity> projects = projectService.findAllByUser(authentication);
        List<ProjectDto> projectDtos = projectMapper.toDtoList(projects);
        return ResponseEntity.ok(projectDtos);
    }

    @Operation(operationId = "getProjectById", summary = "Get project by ID", description = "Retrieves a project by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectDto> findById(@PathVariable UUID id, Authentication authentication) {
        Optional<ProjectEntity> project = projectService.findByIdAndUser(id, authentication);
        return project.map(p -> ResponseEntity.ok(projectMapper.toDto(p)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(operationId = "updateProject", summary = "Update project", description = "Updates an existing project for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectDto.class))),
            @ApiResponse(responseCode = "404", description = "Project not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectDto> update(@PathVariable UUID id, @Valid @RequestBody ProjectUpdateRequestDto projectRequest, Authentication authentication) {
        if (!projectService.existsByIdAndUser(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        ProjectEntity project = projectMapper.toEntity(projectRequest);
        project.setId(id);
        ProjectEntity updatedProject = projectService.update(project);
        ProjectDto projectDto = projectMapper.toDto(updatedProject);
        return ResponseEntity.ok(projectDto);
    }

    @Operation(operationId = "deleteProject", summary = "Delete project", description = "Deletes a project by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        if (!projectService.existsByIdAndUser(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(operationId = "getProjectCount", summary = "Get project count", description = "Retrieves the total number of projects for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Long> count(Authentication authentication) {
        long count = projectService.countByUser(authentication);
        return ResponseEntity.ok(count);
    }
}
