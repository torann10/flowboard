package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * Request DTO for creating a new task.
 * Contains all required and optional fields for task creation.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskCreateRequestDto {
    
    /** The name of the task (required, 2-200 characters) */
    @NotBlank(message = "Task name is required")
    @Size(min = 2, max = 200, message = "Task name must be between 2 and 200 characters")
    private String name;
    
    /** The description of the task (max 1000 characters) */
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    /** The unique identifier of the project this task belongs to */
    private UUID projectId;
    
    /** The unique identifier of the user to assign this task to */
    private UUID assignedToId;
    
    /** The unique identifier of the story point mapping for this task */
    private UUID storyPointMappingId;
    
    /** The initial status of the task (required) */
    @NotNull(message = "Task status is required")
    private TaskStatus status;
}



