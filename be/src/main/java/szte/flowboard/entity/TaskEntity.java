package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.TaskStatus;
import java.time.Duration;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "tasks")
@AllArgsConstructor
@NoArgsConstructor
public class TaskEntity extends AuditEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "booked_time")
    private Duration bookedTime;

    @Column(name = "estimated_time")
    private Duration estimatedTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
}
