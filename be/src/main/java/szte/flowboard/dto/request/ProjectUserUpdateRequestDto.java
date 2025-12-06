package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;
import jakarta.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserUpdateRequestDto {
    
    @NotNull(message = "User role is required")
    private UserRole role;

    @NotNull(message = "User fee is required")
    private Double fee;
}

