package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.Duration;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "story_point_time_mappings")
@AllArgsConstructor
@NoArgsConstructor
public class StoryPointTimeMapping extends AuditEntity {

    @Column(name = "story_points", nullable = false)
    private Integer storyPoints;

    @Column(name = "time_value", nullable = false)
    private Duration timeValue;

    @ManyToOne
    @JoinColumn(name = "project_id", referencedColumnName = "id")
    private ProjectEntity project;
}
