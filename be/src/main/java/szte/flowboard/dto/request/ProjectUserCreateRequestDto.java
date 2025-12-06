package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserCreateRequestDto {
    
    private UUID projectId;
    
    private UUID userId;
    
    @NotNull(message = "User role is required")
    private UserRole role;

    @NotNull(message = "User fee is required")
    private Double fee;
}



