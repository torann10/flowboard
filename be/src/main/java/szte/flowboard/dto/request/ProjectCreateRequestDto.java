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

import java.util.List;

/**
 * Request DTO for creating a new project.
 * Contains all required and optional fields for project creation.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectCreateRequestDto {
    
    /** The name of the project (required, 2-100 characters) */
    @NotBlank(message = "Project name is required")
    @Size(min = 2, max = 100, message = "Project name must be between 2 and 100 characters")
    private String name;
    
    /** The initial status of the project (required) */
    @NotNull(message = "Project status is required")
    private ProjectStatus status;
    
    /** The type of the project (required) */
    @NotNull(message = "Project type is required")
    private ProjectType type;

    /** The fee per story point (only applicable for STORY_POINT_BASED projects) */
    private Double storyPointFee;
    
    /** The list of story point to time mappings for the project */
    private List<StoryPointTimeMappingDto> storyPointTimeMappings;

    /** The customer company information (required) */
    @NotNull
    private CompanyDto customer;

    /** The contractor company information (required) */
    @NotNull
    private CompanyDto contractor;
}



