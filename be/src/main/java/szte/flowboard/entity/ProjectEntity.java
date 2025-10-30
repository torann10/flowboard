package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.ProjectStatus;
import szte.flowboard.enums.ProjectType;
import java.time.Duration;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "projects")
@AllArgsConstructor
@NoArgsConstructor
public class ProjectEntity extends AuditEntity {

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProjectType type;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<StoryPointTimeMapping> storyPointTimeMappings;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectUserEntity> projectUsers;
}
