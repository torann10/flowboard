package szte.flowboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.service.ProjectService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/projects")
public class ProjectController {

    private final ProjectService projectService;

    @PostMapping
    public ResponseEntity<ProjectEntity> create(@RequestBody ProjectEntity project) {
        ProjectEntity createdProject = projectService.create(project);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProject);
    }

    @GetMapping
    public ResponseEntity<List<ProjectEntity>> findAll() {
        List<ProjectEntity> projects = projectService.findAll();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectEntity> findById(@PathVariable UUID id) {
        Optional<ProjectEntity> project = projectService.findById(id);
        return project.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectEntity> update(@PathVariable UUID id, @RequestBody ProjectEntity project) {
        if (!projectService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        project.setId(id);
        ProjectEntity updatedProject = projectService.update(project);
        return ResponseEntity.ok(updatedProject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!projectService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        projectService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = projectService.count();
        return ResponseEntity.ok(count);
    }
}
