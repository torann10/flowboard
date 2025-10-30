package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskUpdateRequestDto {
    
    @NotBlank(message = "Task name is required")
    @Size(min = 2, max = 200, message = "Task name must be between 2 and 200 characters")
    private String name;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotBlank(message = "Project ID is required")
    private String projectId;
    
    private String assignTo;
    
    private String estimatedTime; // ISO 8601 duration format
    
    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 100, message = "Story points must be at most 100")
    private Integer storyPoints;
    
    @NotNull(message = "Task status is required")
    private TaskStatus status;
}

