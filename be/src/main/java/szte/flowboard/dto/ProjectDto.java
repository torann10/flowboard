package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.ProjectStatus;
import szte.flowboard.enums.ProjectType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Object for project information.
 * Used to transfer project data between the API layer and client applications.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    
    /** The unique identifier of the project */
    private UUID id;
    
    /** The name of the project */
    private String name;
    
    /** The current status of the project */
    private ProjectStatus status;
    
    /** The type of the project (TIME_BASED or STORY_POINT_BASED) */
    private ProjectType type;
    
    /** The fee per story point (only applicable for STORY_POINT_BASED projects) */
    private Double storyPointFee;
    
    /** The list of story point to time mappings for the project */
    private List<StoryPointTimeMappingDto> storyPointTimeMappings;
    
    /** The username of the user who created the project */
    private String createdBy;
    
    /** The timestamp when the project was created */
    private LocalDateTime createdAt;
    
    /** The username of the user who last modified the project */
    private String lastModifiedBy;
    
    /** The timestamp when the project was last modified */
    private LocalDateTime lastModifiedAt;
    
    /** The customer company information */
    private CompanyDto customer;
    
    /** The contractor company information */
    private CompanyDto contractor;
}

