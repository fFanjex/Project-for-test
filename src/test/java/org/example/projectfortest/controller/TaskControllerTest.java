package org.example.projectfortest.controller;

import org.example.projectfortest.dto.UpdateTaskDTO;
import org.example.projectfortest.entity.Task;
import org.example.projectfortest.entity.enums.Priority;
import org.example.projectfortest.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_shouldReturnOkWithTask() {
        Task task = new Task();
        task.setTitle("Test Task");
        when(taskService.createTask(task)).thenReturn(task);
        ResponseEntity<?> response = taskController.createTask(task);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(task);
        verify(taskService, times(1)).createTask(task);
    }

    @Test
    void deleteTask_shouldReturnOk() {
        UUID taskId = UUID.randomUUID();
        doNothing().when(taskService).deleteTask(taskId);
        ResponseEntity<?> response = taskController.deleteTask(taskId);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(taskService, times(1)).deleteTask(taskId);
    }

    @Test
    void updateTask_shouldReturnOkWithUpdatedTask() {
        UUID taskId = UUID.randomUUID();
        UpdateTaskDTO updateTaskDTO = new UpdateTaskDTO(
                "Updated title",
                "Updated description",
                null,
                null,
                null
        );
        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setTitle(updateTaskDTO.getTitle());
        updatedTask.setDescription(updateTaskDTO.getDescription());
        when(taskService.updateTask(taskId, updateTaskDTO)).thenReturn(updatedTask);
        ResponseEntity<?> response = taskController.updateTask(taskId, updateTaskDTO);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isEqualTo(updatedTask);
        verify(taskService, times(1)).updateTask(taskId, updateTaskDTO);
    }

    @Test
    void doneTask_shouldReturnOkAndCallService() {
        UUID taskId = UUID.randomUUID();
        doNothing().when(taskService).updateTaskStatusToDone(taskId);
        ResponseEntity<Void> response = taskController.doneTask(taskId);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(taskService, times(1)).updateTaskStatusToDone(taskId);
    }

    @Test
    void inProgressTask_shouldReturnOkAndCallService() {
        UUID taskId = UUID.randomUUID();
        doNothing().when(taskService).updateTaskStatusToInProgress(taskId);
        ResponseEntity<Void> response = taskController.inProgressTask(taskId);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        verify(taskService, times(1)).updateTaskStatusToInProgress(taskId);
    }

    @Test
    void getAllTasks_shouldReturnListOfTasks() {
        Task task1 = new Task();
        task1.setTitle("Task 1");
        Task task2 = new Task();
        task2.setTitle("Task 2");
        when(taskService.getAllTasks()).thenReturn(List.of(task1, task2));
        ResponseEntity<?> response = taskController.getAllTasks();
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(List.class);
        List<?> tasks = (List<?>) response.getBody();
        assertThat(tasks).hasSize(2);
        assertThat(tasks)
                .extracting(task -> ((Task) task).getTitle())
                .containsExactlyInAnyOrder("Task 1", "Task 2");
        verify(taskService, times(1)).getAllTasks();
    }

    @Test
    void filterTasksEndpoint_shouldReturnFilteredTasks() {
        Task task = new Task();
        task.setTitle("Task One");
        when(taskService.filterTasks("one", null, null, null, null)).thenReturn(List.of(task));
        ResponseEntity<?> response = taskController.filterTasks("one", null, null, null, null);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        List<?> body = (List<?>) response.getBody();
        assertThat(body).hasSize(1);
        assertThat(((Task) body.get(0)).getTitle()).isEqualTo("Task One");
        verify(taskService, times(1)).filterTasks("one", null, null, null, null);
    }

    @Test
    void sortTasksEndpoint_shouldReturnSortedTasks() {
        Task task1 = new Task(); task1.setId(UUID.randomUUID()); task1.setPriority(Priority.HIGH);
        Task task2 = new Task(); task2.setId(UUID.randomUUID()); task2.setPriority(Priority.LOW);
        when(taskService.getTaskById(task1.getId())).thenReturn(Optional.of(task1));
        when(taskService.getTaskById(task2.getId())).thenReturn(Optional.of(task2));
        when(taskService.sortTasks(List.of(task1, task2), "priority", true)).thenReturn(List.of(task2, task1));
        ResponseEntity<?> response = taskController.sortTasks(
                List.of(task1.getId(), task2.getId()), "priority", true
        );
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        List<?> body = (List<?>) response.getBody();
        assertThat(((Task) body.get(0)).getPriority()).isEqualTo(Priority.LOW);
        assertThat(((Task) body.get(1)).getPriority()).isEqualTo(Priority.HIGH);
        verify(taskService, times(1)).sortTasks(List.of(task1, task2), "priority", true);
    }
}