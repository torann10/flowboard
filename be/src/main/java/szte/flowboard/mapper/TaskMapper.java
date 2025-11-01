package szte.flowboard.mapper;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import szte.flowboard.dto.TaskDto;
import szte.flowboard.dto.TaskCreateRequestDto;
import szte.flowboard.dto.TaskUpdateRequestDto;
import szte.flowboard.entity.*;

import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper implements EntityMapper<TaskEntity, TaskDto> {

    private final EntityManager entityManager;

    public TaskMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public TaskDto toDto(TaskEntity entity) {
        if (entity == null) {
            return null;
        }

        TaskDto dto = new TaskDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setProjectId(entity.getProject().getId());

        if (entity.getAssignedTo() != null) {
            dto.setAssignedToId(entity.getAssignedTo().getId());
            dto.setAssignedToName(entity.getAssignedTo().getFirstName() + " " + entity.getAssignedTo().getLastName());
        }

        dto.setBookedTime(entity.getTimeLogs().stream().map(TimeLogEntity::getLoggedTime).reduce(Duration.ZERO, Duration::plus));

        dto.setStoryPointMappingId(entity.getStoryPointMapping().getId());
        dto.setStatus(entity.getStatus());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedAt(entity.getLastModifiedAt());

        return dto;
    }

    @Override
    public TaskEntity toEntity(TaskDto dto) {
        if (dto == null) {
            return null;
        }

        TaskEntity entity = new TaskEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setProject(entityManager.getReference(ProjectEntity.class, dto.getProjectId()));

        if (dto.getAssignedToId() != null) {
            entity.setAssignedTo(entityManager.getReference(UserEntity.class, dto.getAssignedToId()));
        }

        entity.setAssignedTo(entityManager.getReference(UserEntity.class, dto.getAssignedToId()));
        entity.setStoryPointMapping(entityManager.getReference(StoryPointTimeMappingEntity.class, dto.getStoryPointMappingId()));
        entity.setStatus(dto.getStatus());
        entity.setLastModifiedBy(dto.getLastModifiedBy());
        entity.setLastModifiedAt(dto.getLastModifiedAt());

        return entity;
    }

    public TaskEntity toEntity(TaskCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        TaskEntity entity = new TaskEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setProject(entityManager.getReference(ProjectEntity.class, dto.getProjectId()));

        if (dto.getAssignedToId() != null) {
            entity.setAssignedTo(entityManager.getReference(UserEntity.class, dto.getAssignedToId()));
        }

        entity.setStoryPointMapping(entityManager.getReference(StoryPointTimeMappingEntity.class, dto.getStoryPointMappingId()));
        entity.setStatus(dto.getStatus());

        return entity;
    }

    public TaskEntity toEntity(TaskUpdateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        TaskEntity entity = new TaskEntity();
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());

        if (dto.getAssignedToId() != null) {
            entity.setAssignedTo(entityManager.getReference(UserEntity.class, dto.getAssignedToId()));
        }

        entity.setStoryPointMapping(entityManager.getReference(StoryPointTimeMappingEntity.class, dto.getStoryPointMappingId()));
        entity.setStatus(dto.getStatus());

        return entity;
    }

    @Override
    public List<TaskDto> toDtoList(List<TaskEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskEntity> toEntityList(List<TaskDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

