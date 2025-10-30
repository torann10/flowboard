package szte.flowboard.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.TaskStatus;
import java.time.Duration;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TaskDto {
    
    private String id;
    private String name;
    private String description;
    private String projectId;
    private String assignTo;
    private Duration bookedTime;
    private Duration estimatedTime;
    private Integer storyPoints;
    private TaskStatus status;
    private String createdBy;
    private LocalDate createdAt;
    private String lastModifiedBy;
    private LocalDate lastModifiedAt;
}

