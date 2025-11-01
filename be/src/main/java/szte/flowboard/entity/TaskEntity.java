package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.TaskStatus;

import java.time.Duration;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity extends AuditEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectEntity project;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TimeLogEntity> timeLogs;

    @ManyToOne
    @JoinColumn(name = "assigned_to_id", referencedColumnName = "id")
    private UserEntity assignedTo;

    @ManyToOne
    @JoinColumn(name = "story_point_mapping_id", referencedColumnName = "id")
    private StoryPointTimeMappingEntity storyPointMapping;
}
