package org.example.projectfortest.controller;

import lombok.RequiredArgsConstructor;
import org.example.projectfortest.dto.UpdateTaskDTO;
import org.example.projectfortest.entity.Task;
import org.example.projectfortest.service.TaskService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

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
