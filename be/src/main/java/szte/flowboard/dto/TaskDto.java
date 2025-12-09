package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Dictionary;
import java.util.UUID;

/**
 * Data Transfer Object for task information.
 * Used to transfer task data between the API layer and client applications.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    
    /** The unique identifier of the task */
    private UUID id;
    
    /** The name of the task */
    private String name;
    
    /** The description of the task */
    private String description;
    
    /** The unique identifier of the project this task belongs to */
    private UUID projectId;
    
    /** The unique identifier of the user assigned to this task */
    private UUID assignedToId;
    
    /** The full name of the user assigned to this task */
    private String assignedToName;
    
    /** The total time logged for this task */
    private Duration bookedTime;
    
    /** The unique identifier of the story point mapping associated with this task */
    private UUID storyPointMappingId;
    
    /** The current status of the task */
    private TaskStatus status;
    
    /** The username of the user who created the task */
    private String createdBy;
    
    /** The timestamp when the task was created */
    private LocalDateTime createdAt;
    
    /** The username of the user who last modified the task */
    private String lastModifiedBy;
    
    /** The timestamp when the task was last modified */
    private LocalDateTime lastModifiedAt;
}

