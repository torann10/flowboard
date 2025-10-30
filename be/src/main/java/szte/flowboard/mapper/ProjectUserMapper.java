package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.ProjectUserDto;
import szte.flowboard.dto.ProjectUserCreateRequestDto;
import szte.flowboard.dto.ProjectUserUpdateRequestDto;
import szte.flowboard.entity.ProjectUserEntity;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProjectUserMapper implements EntityMapper<ProjectUserEntity, ProjectUserDto> {

    @Override
    public ProjectUserDto toDto(ProjectUserEntity entity) {
        if (entity == null) {
            return null;
        }

        ProjectUserDto dto = new ProjectUserDto();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setProjectId(entity.getProject() != null ? entity.getProject().getId().toString() : null);
        dto.setUserId(entity.getUser() != null ? entity.getUser().getId().toString() : null);
        dto.setRole(entity.getRole());
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
        entity.setId(dto.getId() != null ? UUID.fromString(dto.getId()) : null);
        entity.setRole(dto.getRole());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setLastModifiedBy(dto.getLastModifiedBy());

        return entity;
    }

    public ProjectUserEntity toEntity(ProjectUserCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ProjectUserEntity entity = new ProjectUserEntity();
        // Note: The relationships (project and user) will need to be set by the service
        // since the mapper doesn't have access to repositories
        entity.setRole(dto.getRole());

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
