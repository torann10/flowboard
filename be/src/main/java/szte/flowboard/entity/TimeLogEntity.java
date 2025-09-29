package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Duration;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "time_logs")
@AllArgsConstructor
@NoArgsConstructor
public class TimeLogEntity extends AuditEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "task_id", nullable = false)
    private UUID taskId;

    @Column(name = "logged_time", nullable = false)
    private Duration loggedTime;

    @Column(name = "is_billable", nullable = false)
    private Boolean isBillable;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;
}
