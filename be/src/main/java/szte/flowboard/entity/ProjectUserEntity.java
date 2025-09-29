package szte.flowboard.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import szte.flowboard.enums.UserRole;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "project_users")
@AllArgsConstructor
@NoArgsConstructor
public class ProjectUserEntity extends AuditEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;
}
