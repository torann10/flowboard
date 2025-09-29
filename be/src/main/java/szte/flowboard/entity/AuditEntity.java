package szte.flowboard.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
public class AuditEntity extends BaseEntity {

    @CreatedBy
    private String createdBy;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDate createAt;

    @LastModifiedBy
    private String lastModifiedBy;

    @LastModifiedDate
    private LocalDate lastModifiedAt;
}
