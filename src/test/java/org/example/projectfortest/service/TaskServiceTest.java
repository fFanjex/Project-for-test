package org.example.projectfortest.service;

import org.example.projectfortest.dto.UpdateTaskDTO;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
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

    @Test
    void updateTask_shouldUpdateFieldsAndSave() {
        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO(
                "New title",
                "New description",
                LocalDateTime.now().plusDays(3),
                org.example.projectfortest.entity.enums.Priority.HIGH,
                org.example.projectfortest.entity.enums.Category.WORK
        );
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        Task updatedTask = taskService.updateTask(taskId, updateTaskDTO);
        assertEquals(updateTaskDTO.getTitle(), updatedTask.getTitle());
        assertEquals(updateTaskDTO.getDescription(), updatedTask.getDescription());
        assertEquals(updateTaskDTO.getDueDate(), updatedTask.getDueDate());
        assertEquals(updateTaskDTO.getPriority(), updatedTask.getPriority());
        assertEquals(updateTaskDTO.getCategory(), updatedTask.getCategory());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void updateTask_shouldThrowIfTaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> taskService.updateTask(taskId, new UpdateTaskDTO("t", "d", null, null, null)));
        assertEquals("Task not found", exception.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void updateTaskStatusToDone_shouldSetStatusAndSave() {
        task.setStatus(org.example.projectfortest.entity.enums.TaskStatus.CREATED);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        taskService.updateTaskStatusToDone(taskId);
        assertEquals(org.example.projectfortest.entity.enums.TaskStatus.DONE, task.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void updateTaskStatusToInProgress_shouldSetStatusAndSave() {
        task.setStatus(org.example.projectfortest.entity.enums.TaskStatus.CREATED);
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));
        taskService.updateTaskStatusToInProgress(taskId);
        assertEquals(org.example.projectfortest.entity.enums.TaskStatus.IN_PROGRESS, task.getStatus());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void updateTaskStatus_shouldThrowIfTaskNotFound() {
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());
        RuntimeException exception1 = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskStatusToDone(taskId));
        assertEquals("Task not found", exception1.getMessage());
        RuntimeException exception2 = assertThrows(RuntimeException.class,
                () -> taskService.updateTaskStatusToInProgress(taskId));
        assertEquals("Task not found", exception2.getMessage());
        verify(taskRepository, never()).save(any());
    }

    @Test
    void getAllTasks_shouldReturnTasksForCurrentUser() {
        Task task1 = new Task();
        task1.setUser(user);
        Task task2 = new Task();
        task2.setUser(user);

        when(taskRepository.findByUser(user)).thenReturn(List.of(task1, task2));

        List<Task> tasks = taskService.getAllTasks();

        assertThat(tasks).hasSize(2);
        assertThat(tasks).contains(task1, task2);

        verify(taskRepository, times(1)).findByUser(user);
    }
}