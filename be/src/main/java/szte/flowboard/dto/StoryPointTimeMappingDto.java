package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Duration;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Data Transfer Object for story point to time mapping.
 * Defines the relationship between story points and estimated time duration for story-point-based projects.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoryPointTimeMappingDto {
    
    /** The unique identifier of the story point time mapping */
    private UUID id;
    
    /** The unique identifier of the project this mapping belongs to */
    private UUID projectId;
    
    /** The number of story points (must be between 1 and 100) */
    @NotNull(message = "Story points is required")
    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 100, message = "Story points must be at most 100")
    private Integer storyPoints;
    
    /** The estimated time duration for the story points */
    @NotNull(message = "Time value is required")
    private Duration timeValue;
}
