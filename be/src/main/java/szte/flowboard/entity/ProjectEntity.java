package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.ProjectStatus;

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
}
