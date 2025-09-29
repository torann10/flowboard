package szte.flowboard.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.service.TaskService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/tasks")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    public ResponseEntity<TaskEntity> create(@RequestBody TaskEntity task) {
        TaskEntity createdTask = taskService.create(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping
    public ResponseEntity<List<TaskEntity>> findAll() {
        List<TaskEntity> tasks = taskService.findAll();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskEntity> findById(@PathVariable UUID id) {
        Optional<TaskEntity> task = taskService.findById(id);
        return task.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskEntity> update(@PathVariable UUID id, @RequestBody TaskEntity task) {
        if (!taskService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        task.setId(id);
        TaskEntity updatedTask = taskService.update(task);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!taskService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = taskService.count();
        return ResponseEntity.ok(count);
    }
}
