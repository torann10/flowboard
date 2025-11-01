package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Dictionary;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    
    private UUID id;
    private String name;
    private String description;
    private UUID projectId;
    private UUID assignedToId;
    private String assignedToName;
    private Duration bookedTime;
    private UUID storyPointMappingId;
    private TaskStatus status;
    private String createdBy;
    private LocalDate createdAt;
    private String lastModifiedBy;
    private LocalDate lastModifiedAt;
}

