package org.example.projectfortest.controller;

import org.example.projectfortest.dto.UpdateTaskDTO;
import org.example.projectfortest.entity.Task;
import org.example.projectfortest.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

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
}