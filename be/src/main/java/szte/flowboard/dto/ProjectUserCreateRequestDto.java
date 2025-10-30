package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserCreateRequestDto {
    
    @NotBlank(message = "Project ID is required")
    private String projectId;
    
    @NotBlank(message = "User ID is required")
    private String userId;
    
    @NotNull(message = "User role is required")
    private UserRole role;
}

