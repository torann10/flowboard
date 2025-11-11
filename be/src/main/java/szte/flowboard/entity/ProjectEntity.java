package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.ProjectStatus;
import szte.flowboard.enums.ProjectType;

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
    private List<StoryPointTimeMappingEntity> storyPointTimeMappings;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<ProjectUserEntity> projectUsers;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskEntity> tasks;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column =  @Column(name = "customer_name", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "customer_address", nullable = false))
    })
    private CompanyEntity customer;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "name", column =  @Column(name = "contractor_name", nullable = false)),
            @AttributeOverride(name = "address", column = @Column(name = "contractor_address", nullable = false))
    })
    private CompanyEntity contractor;
}
