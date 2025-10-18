package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.service.ProjectUserService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/project-users")
public class ProjectUserController {

    private final ProjectUserService projectUserService;

    @Operation(summary = "Create project-user relationship", description = "Creates a new project-user relationship")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Project-user relationship created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ProjectUserEntity> create(@RequestBody ProjectUserEntity projectUser) {
        ProjectUserEntity createdProjectUser = projectUserService.create(projectUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProjectUser);
    }

    @Operation(summary = "Get all project-user relationships", description = "Retrieves all project-user relationships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project-user relationships retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ProjectUserEntity>> findAll() {
        List<ProjectUserEntity> projectUsers = projectUserService.findAll();
        return ResponseEntity.ok(projectUsers);
    }

    @Operation(summary = "Get project-user relationship by ID", description = "Retrieves a project-user relationship by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project-user relationship found successfully"),
            @ApiResponse(responseCode = "404", description = "Project-user relationship not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProjectUserEntity> findById(@PathVariable UUID id) {
        Optional<ProjectUserEntity> projectUser = projectUserService.findById(id);
        return projectUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update project-user relationship", description = "Updates an existing project-user relationship")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Project-user relationship updated successfully"),
            @ApiResponse(responseCode = "404", description = "Project-user relationship not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProjectUserEntity> update(@PathVariable UUID id, @RequestBody ProjectUserEntity projectUser) {
        if (!projectUserService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectUser.setId(id);
        ProjectUserEntity updatedProjectUser = projectUserService.update(projectUser);
        return ResponseEntity.ok(updatedProjectUser);
    }

    @Operation(summary = "Delete project-user relationship", description = "Deletes a project-user relationship by its ID")
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

    @Operation(summary = "Get project-user relationship count", description = "Retrieves the total number of project-user relationships")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = projectUserService.count();
        return ResponseEntity.ok(count);
    }
}
