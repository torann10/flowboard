package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Object for time log entry information.
 * Used to transfer time log data between the API layer and client applications.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogDto {
    
    /** The unique identifier of the time log entry */
    private UUID id;
    
    /** The unique identifier of the task this time log belongs to */
    private UUID taskId;
    
    /** The unique identifier of the user who logged the time */
    private UUID userId;
    
    /** The amount of time logged */
    private Duration loggedTime;
    
    /** Whether this time log entry is billable */
    private boolean isBillable;
    
    /** The date when the time was logged */
    private LocalDate logDate;
    
    /** The username of the user who created the time log entry */
    private String createdBy;
    
    /** The timestamp when the time log entry was created */
    private LocalDateTime createdAt;
    
    /** The username of the user who last modified the time log entry */
    private String lastModifiedBy;
    
    /** The timestamp when the time log entry was last modified */
    private LocalDateTime lastModifiedAt;
}

