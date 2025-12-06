package szte.flowboard.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.ProjectStatus;
import szte.flowboard.enums.ProjectType;
import szte.flowboard.dto.CompanyDto;
import szte.flowboard.dto.StoryPointTimeMappingDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUpdateRequestDto {
    
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;
    
    @NotNull(message = "Project status is required")
    private ProjectStatus status;
    
    @NotNull(message = "Project type is required")
    private ProjectType type;

    private Double storyPointFee;
    
    private java.util.List<StoryPointTimeMappingDto> storyPointTimeMappings;

    @NotNull
    private CompanyDto customer;

    @NotNull
    private CompanyDto contractor;
}

