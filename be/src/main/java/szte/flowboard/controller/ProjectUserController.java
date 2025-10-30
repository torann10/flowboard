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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.dto.ProjectUserDto;
import szte.flowboard.dto.ProjectUserCreateRequestDto;
import szte.flowboard.dto.ProjectUserUpdateRequestDto;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.mapper.ProjectUserMapper;
import szte.flowboard.service.ProjectUserService;
import szte.flowboard.service.ProjectService;
import szte.flowboard.service.UserService;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.UserEntity;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/project-users")
public class ProjectUserController {

    private final ProjectUserService projectUserService;
    private final ProjectUserMapper projectUserMapper;
    private final ProjectService projectService;
    private final UserService userService;

    @Operation(operationId = "createProjectUser", summary = "Create project-user relationship", description = "Creates a new project-user relationship")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project-user relationship created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectUserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ProjectUserDto> create(@Valid @RequestBody ProjectUserCreateRequestDto projectUserRequest, Authentication authentication) {
        ProjectUserEntity projectUser = projectUserMapper.toEntity(projectUserRequest);

        // Set the relationships
        Optional<ProjectEntity> project = projectService.findByIdAndUser(UUID.fromString(projectUserRequest.getProjectId()), authentication);
        Optional<UserEntity> user = userService.findByIdEntity(UUID.fromString(projectUserRequest.getUserId()));
        
        if (project.isEmpty() || user.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        projectUser.setProject(project.get());
        projectUser.setUser(user.get());
        
        ProjectUserEntity createdProjectUser = projectUserService.create(projectUser);
        ProjectUserDto projectUserDto = projectUserMapper.toDto(createdProjectUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectUserDto);
    }

    @Operation(operationId = "getAllProjectUsers", summary = "Get all project-user relationships", description = "Retrieves all project-user relationships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project-user relationships retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ProjectUserDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<ProjectUserDto>> findAll() {
        List<ProjectUserEntity> projectUsers = projectUserService.findAll();
        List<ProjectUserDto> projectUserDtos = projectUserMapper.toDtoList(projectUsers);
        return ResponseEntity.ok(projectUserDtos);
    }

    @Operation(operationId = "getProjectUserById", summary = "Get project-user relationship by ID", description = "Retrieves a project-user relationship by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project-user relationship found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectUserDto.class))),
            @ApiResponse(responseCode = "404", description = "Project-user relationship not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectUserDto> findById(@PathVariable UUID id) {
        Optional<ProjectUserEntity> projectUser = projectUserService.findById(id);
        return projectUser.map(pu -> ResponseEntity.ok(projectUserMapper.toDto(pu)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(operationId = "updateProjectUser", summary = "Update project-user relationship", description = "Updates an existing project-user relationship")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project-user relationship updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectUserDto.class))),
            @ApiResponse(responseCode = "404", description = "Project-user relationship not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectUserDto> update(@PathVariable UUID id, @Valid @RequestBody ProjectUserUpdateRequestDto projectUserRequest) {
        if (!projectUserService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        ProjectUserEntity updatedProjectUser = projectUserService.update(id, projectUserRequest.getRole());
        ProjectUserDto projectUserDto = projectUserMapper.toDto(updatedProjectUser);
        return projectUserDto == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(projectUserDto);
    }

    @Operation(operationId = "deleteProjectUser", summary = "Delete project-user relationship", description = "Deletes a project-user relationship by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Project-user relationship deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Project-user relationship not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!projectUserService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(operationId = "getProjectUserCount", summary = "Get project-user relationship count", description = "Retrieves the total number of project-user relationships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = projectUserService.count();
        return ResponseEntity.ok(count);
    }
}
