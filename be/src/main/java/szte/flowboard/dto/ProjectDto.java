package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.ProjectStatus;
import szte.flowboard.enums.ProjectType;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ProjectDto {
    
    private UUID id;
    private String name;
    private ProjectStatus status;
    private ProjectType type;
    private List<StoryPointTimeMappingDto> storyPointTimeMappings;
    private String createdBy;
    private LocalDate createAt;
    private String lastModifiedBy;
    private LocalDate lastModifiedAt;
}

