package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.PastOrPresent;
import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReportCreateRequestDto {
    
    @NotBlank(message = "Project ID is required")
    private String projectId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    private String taskId;
    
    @NotBlank(message = "Description is required")
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    @NotNull(message = "Report date is required")
    @PastOrPresent(message = "Report date cannot be in the future")
    private LocalDateTime reportDate;
}

