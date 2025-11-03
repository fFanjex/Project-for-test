package org.example.projectfortest.service;

import lombok.RequiredArgsConstructor;
import org.example.projectfortest.dto.UpdateTaskDTO;
import org.example.projectfortest.entity.Task;
import org.example.projectfortest.entity.User;
import org.example.projectfortest.entity.enums.TaskStatus;
import org.example.projectfortest.repository.TaskRepository;
import org.example.projectfortest.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public Task createTask(Task task) {
        User currentUser = getCurrentUser();
        task.setUser(currentUser);
        return taskRepository.save(task);
    }

    public void deleteTask(UUID taskId) {
        User currentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new RuntimeException("Task not found"));
        if (!task.getUser().equals(currentUser)) {
            throw new RuntimeException("You are not allowed to delete this task");
        }
        taskRepository.delete(task);
    }

    public Task updateTask(UUID taskId, UpdateTaskDTO updateTaskDTO) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new RuntimeException("Task not found")
        );
        task.setTitle(updateTaskDTO.getTitle());
        task.setDescription(updateTaskDTO.getDescription());
        task.setDueDate(updateTaskDTO.getDueDate());
        task.setPriority(updateTaskDTO.getPriority());
        task.setCategory(updateTaskDTO.getCategory());
        return taskRepository.save(task);
    }

    public void updateTaskStatusToInProgress(UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new RuntimeException("Task not found")
        );
        task.setStatus(TaskStatus.IN_PROGRESS);
        taskRepository.save(task);
    }

    public void updateTaskStatusToDone(UUID taskId) {
        Task task = taskRepository.findById(taskId).orElseThrow(
                () -> new RuntimeException("Task not found")
        );
        task.setStatus(TaskStatus.DONE);
        taskRepository.save(task);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
