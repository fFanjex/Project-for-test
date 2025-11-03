package org.example.projectfortest.service;

import lombok.RequiredArgsConstructor;
import org.example.projectfortest.entity.Task;
import org.example.projectfortest.entity.User;
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
        User cuurentUser = getCurrentUser();
        Task task = taskRepository.findById(taskId).orElseThrow(() ->
                new RuntimeException("Task not found"));
        if (!task.getUser().equals(cuurentUser)) {
            throw new RuntimeException("You are not allowed to delete this task");
        }
        taskRepository.delete(task);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
