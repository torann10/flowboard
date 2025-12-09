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
import szte.flowboard.dto.ProjectUserDto;
import szte.flowboard.dto.request.ProjectUserCreateRequestDto;
import szte.flowboard.dto.request.ProjectUserUpdateRequestDto;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.mapper.ProjectUserMapper;
import szte.flowboard.service.ProjectUserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * REST controller for managing project-user relationships.
 * Provides endpoints for creating, reading, updating, and deleting project-user associations.
 * These relationships define which users have access to which projects and their roles.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/project-users")
public class ProjectUserController {

    private final ProjectUserService projectUserService;
    private final ProjectUserMapper projectUserMapper;

    /**
     * Creates a new project-user relationship, assigning a user to a project with a specific role.
     *
     * @param projectUserRequest the project-user creation request containing project, user, role, and fee details
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the created project-user DTO with HTTP status 201, or 400 if input is invalid
     */
    @Operation(operationId = "createProjectUser", summary = "Create project-user relationship", description = "Creates a new project-user relationship")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project-user relationship created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ProjectUserDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ProjectUserDto> create(@Valid @RequestBody ProjectUserCreateRequestDto projectUserRequest, Authentication authentication) {
        ProjectUserEntity projectUser = projectUserMapper.toEntity(projectUserRequest);
        ProjectUserEntity createdProjectUser = projectUserService.create(projectUser, authentication);
        ProjectUserDto projectUserDto = projectUserMapper.toDto(createdProjectUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(projectUserDto);
    }

    /**
     * Retrieves all project-user relationships in the system.
     *
     * @return ResponseEntity containing a list of project-user DTOs with HTTP status 200
     */
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

    /**
     * Retrieves a project-user relationship by its ID.
     *
     * @param id the unique identifier of the project-user relationship
     * @return ResponseEntity containing the project-user DTO with HTTP status 200 if found, or 404 if not found
     */
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

    /**
     * Updates an existing project-user relationship, allowing changes to role and fee.
     *
     * @param id the unique identifier of the project-user relationship to update
     * @param projectUserRequest the project-user update request containing updated role and fee
     * @return ResponseEntity containing the updated project-user DTO with HTTP status 200, 404 if not found, or 400 if input is invalid
     */
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
        ProjectUserEntity updatedProjectUser = projectUserService.update(id, projectUserRequest.getRole(), projectUserRequest.getFee());
        ProjectUserDto projectUserDto = projectUserMapper.toDto(updatedProjectUser);
        return projectUserDto == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok(projectUserDto);
    }

    /**
     * Deletes a project-user relationship by its ID, removing the user's access to the project.
     *
     * @param id the unique identifier of the project-user relationship to delete
     * @return ResponseEntity with HTTP status 204 if deleted successfully, or 404 if not found
     */
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
}
