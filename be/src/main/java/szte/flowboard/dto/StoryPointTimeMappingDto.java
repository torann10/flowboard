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

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class StoryPointTimeMappingDto {
    
    private UUID id;
    private UUID projectId;
    
    @NotNull(message = "Story points is required")
    @Min(value = 1, message = "Story points must be at least 1")
    @Max(value = 100, message = "Story points must be at most 100")
    private Integer storyPoints;
    
    @NotNull(message = "Time value is required")
    private Duration timeValue;
}
