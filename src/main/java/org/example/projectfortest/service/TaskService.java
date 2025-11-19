package org.example.projectfortest.service;

import lombok.RequiredArgsConstructor;
import org.example.projectfortest.dto.UpdateTaskDTO;
import org.example.projectfortest.entity.Task;
import org.example.projectfortest.entity.User;
import org.example.projectfortest.entity.enums.Category;
import org.example.projectfortest.entity.enums.Priority;
import org.example.projectfortest.entity.enums.TaskStatus;
import org.example.projectfortest.repository.TaskRepository;
import org.example.projectfortest.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    public List<Task> filterTasks(String keyword, Category category, Priority priority, TaskStatus status, Boolean overdue) {
        User currentUser = getCurrentUser();
        List<Task> tasks = taskRepository.findByUser(currentUser);
        return tasks.stream()
                .filter(task -> keyword == null || keyword.isEmpty() ||
                        task.getTitle().toLowerCase().contains(keyword.toLowerCase()) ||
                        (task.getDescription() != null && task.getDescription().toLowerCase().contains(keyword.toLowerCase())))
                .filter(task -> category == null || task.getCategory() == category)
                .filter(task -> priority == null || task.getPriority() == priority)
                .filter(task -> status == null || task.getStatus() == status)
                .filter(task -> overdue == null || !overdue || task.isOverdue())
                .collect(Collectors.toList());
    }

    public List<Task> sortTasks(List<Task> tasks, String sortBy, boolean ascending) {
        Comparator<Task> comparator;
        switch (sortBy != null ? sortBy : "") {
            case "dueDate":
                comparator = Comparator.comparing(Task::getDueDate,
                        Comparator.nullsLast(Comparator.naturalOrder()));
                break;
            case "status":
                comparator = Comparator.comparing(Task::getStatus);
                break;
            case "priority":
            default:
                comparator = Comparator.comparing(Task::getPriority);
                break;
        }
        if (!ascending) {
            comparator = comparator.reversed();
        }
        tasks.sort(comparator);
        return tasks;
    }

    public List<Task> getAllTasks() {
        User currentUser = getCurrentUser();
        return taskRepository.findByUser(currentUser);
    }

    public Optional<Task> getTaskById(UUID taskId) {
        return taskRepository.findById(taskId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
