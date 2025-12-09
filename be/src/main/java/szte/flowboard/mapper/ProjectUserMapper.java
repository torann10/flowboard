package szte.flowboard.mapper;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import szte.flowboard.dto.ProjectUserDto;
import szte.flowboard.dto.request.ProjectUserCreateRequestDto;
import szte.flowboard.entity.ProjectEntity;
import szte.flowboard.entity.ProjectUserEntity;
import szte.flowboard.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ProjectUserMapper implements EntityMapper<ProjectUserEntity, ProjectUserDto> {

    private final EntityManager entityManager;

    public ProjectUserMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public ProjectUserDto toDto(ProjectUserEntity entity) {
        if (entity == null) {
            return null;
        }

        ProjectUserDto dto = new ProjectUserDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setRole(entity.getRole());
        dto.setFee(entity.getFee());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedAt(entity.getLastModifiedAt());

        return dto;
    }

    @Override
    public ProjectUserEntity toEntity(ProjectUserDto dto) {
        if (dto == null) {
            return null;
        }

        ProjectUserEntity entity = new ProjectUserEntity();
        entity.setId(dto.getId());
        entity.setUser(entityManager.getReference(UserEntity.class, dto.getUserId()));
        entity.setProject(entityManager.getReference(ProjectEntity.class, dto.getProjectId()));
        entity.setRole(dto.getRole());
        entity.setFee(dto.getFee());

        return entity;
    }

    public ProjectUserEntity toEntity(ProjectUserCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ProjectUserEntity entity = new ProjectUserEntity();
        entity.setUser(entityManager.getReference(UserEntity.class, dto.getUserId()));
        entity.setProject(entityManager.getReference(ProjectEntity.class, dto.getProjectId()));
        entity.setRole(dto.getRole());
        entity.setFee(dto.getFee());

        return entity;
    }

    @Override
    public List<ProjectUserDto> toDtoList(List<ProjectUserEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectUserEntity> toEntityList(List<ProjectUserDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
