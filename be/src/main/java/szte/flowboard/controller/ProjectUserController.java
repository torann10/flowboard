package szte.flowboard.controller;

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

    @PostMapping
    public ResponseEntity<ProjectUserEntity> create(@RequestBody ProjectUserEntity projectUser) {
        ProjectUserEntity createdProjectUser = projectUserService.create(projectUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProjectUser);
    }

    @GetMapping
    public ResponseEntity<List<ProjectUserEntity>> findAll() {
        List<ProjectUserEntity> projectUsers = projectUserService.findAll();
        return ResponseEntity.ok(projectUsers);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectUserEntity> findById(@PathVariable UUID id) {
        Optional<ProjectUserEntity> projectUser = projectUserService.findById(id);
        return projectUser.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectUserEntity> update(@PathVariable UUID id, @RequestBody ProjectUserEntity projectUser) {
        if (!projectUserService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectUser.setId(id);
        ProjectUserEntity updatedProjectUser = projectUserService.update(projectUser);
        return ResponseEntity.ok(updatedProjectUser);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!projectUserService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectUserService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = projectUserService.count();
        return ResponseEntity.ok(count);
    }
}
