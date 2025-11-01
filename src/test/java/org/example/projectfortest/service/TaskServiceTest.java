package org.example.projectfortest.service;

import org.example.projectfortest.entity.Task;
import org.example.projectfortest.entity.User;
import org.example.projectfortest.repository.TaskRepository;
import org.example.projectfortest.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private TaskService taskService;

    private User user;
    private Task task;
    private UUID taskId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setEmail("test@example.com");

        taskId = UUID.randomUUID();
        task = new Task();
        task.setId(taskId);
        task.setUser(user);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
        when(authentication.getName()).thenReturn(user.getEmail());
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
    }

    @Test
    void createTask_shouldAssignUserAndSave() {
        when(taskRepository.save(task)).thenReturn(task);
        Task savedTask = taskService.createTask(task);
        assertEquals(user, savedTask.getUser());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void deleteTask_shouldDeleteTaskIfOwner() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        taskService.deleteTask(taskId);
        verify(taskRepository, times(1)).delete(task);
    }

    @Test
    void deleteTask_shouldThrowIfNotOwner() {
        Task otherTask = new Task();
        otherTask.setId(taskId);
        otherTask.setUser(new User());
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(otherTask));
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.deleteTask(taskId));
        assertEquals("You are not allowed to delete this task", exception.getMessage());
    }

    @Test
    void deleteTask_shouldThrowIfTaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class, () -> taskService.deleteTask(taskId));
        assertEquals("Task not found", exception.getMessage());
    }
}