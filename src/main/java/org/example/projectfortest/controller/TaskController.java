package org.example.projectfortest.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectfortest.dto.UpdateTaskDTO;
import org.example.projectfortest.entity.Task;
import org.example.projectfortest.entity.enums.Category;
import org.example.projectfortest.entity.enums.Priority;
import org.example.projectfortest.entity.enums.TaskStatus;
import org.example.projectfortest.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/task")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    @PostMapping("/add")
    public ResponseEntity<?> createTask(@RequestBody Task task) {
        return ResponseEntity.ok(taskService.createTask(task));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filter")
    public ResponseEntity<?> filterTasks(@RequestParam(required = false) String keyword, @RequestParam(required = false) Category category,
                                         @RequestParam(required = false) Priority priority, @RequestParam(required = false) TaskStatus status,
                                         @RequestParam(required = false) Boolean overdue) {
        return ResponseEntity.ok(taskService.filterTasks(keyword, category, priority, status, overdue));
    }

    @GetMapping("/sort")
    public ResponseEntity<?> sortTasks(@RequestParam List<UUID> taskIds, @RequestParam(required = false) String sortBy, @RequestParam(defaultValue = "true") boolean ascending) {
        List<Task> tasks = taskIds.stream()
                .map(id -> taskService.getTaskById(id)
                        .orElseThrow(() -> new RuntimeException("Task not found: " + id)))
                .collect(Collectors.toList());
        return ResponseEntity.ok(taskService.sortTasks(tasks, sortBy, ascending));
    }

    @PutMapping("/edit/{id}")
    public ResponseEntity<?> updateTask(@PathVariable UUID id,
                                        @RequestBody UpdateTaskDTO updateTaskDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, updateTaskDTO));
    }

    @PostMapping("/in_progress/{id}")
    public ResponseEntity<Void> inProgressTask(@PathVariable UUID id) {
        taskService.updateTaskStatusToInProgress(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/done/{id}")
    public ResponseEntity<Void> doneTask(@PathVariable UUID id) {
        taskService.updateTaskStatusToDone(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllTasks() {
        return ResponseEntity.ok(taskService.getAllTasks());
    }
}
