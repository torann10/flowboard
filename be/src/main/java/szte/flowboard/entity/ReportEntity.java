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
@Table(name = "reports")
@AllArgsConstructor
@NoArgsConstructor
public class ReportEntity extends AuditEntity {

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "interval_from", nullable = false)
    private LocalDate intervalFrom;

    @Column(name = "interval_until", nullable = false)
    private LocalDate intervalUntil;

    @Column(name = "total_time", nullable = false)
    private Duration totalTime;

    @Column(name = "total_billable_time", nullable = false)
    private Duration totalBillableTime;

    @Column(columnDefinition = "TEXT")
    private String data;
}
