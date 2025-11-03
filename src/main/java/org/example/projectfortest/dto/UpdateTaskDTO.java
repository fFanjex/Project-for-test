package org.example.projectfortest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.projectfortest.entity.enums.Category;
import org.example.projectfortest.entity.enums.Priority;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UpdateTaskDTO {
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private Priority priority;
    private Category category;
}
