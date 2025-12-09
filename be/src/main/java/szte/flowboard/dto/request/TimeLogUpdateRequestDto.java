package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating or updating a time log entry.
 * Used for both creation and update operations.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogUpdateRequestDto {
    
    /** The unique identifier of the task this time log belongs to */
    private UUID taskId;

    /** The amount of time logged */
    private Duration loggedTime;

    /** The date when the time was logged (required, cannot be in the future) */
    @NotNull(message = "Log date is required")
    @PastOrPresent(message = "Log date cannot be in the future")
    private LocalDate logDate;

    /** Whether this time log entry is billable (required) */
    @NotNull(message = "Is billable is required")
    private boolean isBillable;
}



