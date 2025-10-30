package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.TaskDto;
import szte.flowboard.dto.TaskCreateRequestDto;
import szte.flowboard.dto.TaskUpdateRequestDto;
import szte.flowboard.entity.TaskEntity;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TaskMapper implements EntityMapper<TaskEntity, TaskDto> {

    @Override
    public TaskDto toDto(TaskEntity entity) {
        if (entity == null) {
            return null;
        }

        TaskDto dto = new TaskDto();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        dto.setProjectId(entity.getProjectId() != null ? entity.getProjectId().toString() : null);
        dto.setAssignTo(entity.getAssignTo() != null ? entity.getAssignTo().toString() : null);
        dto.setBookedTime(entity.getBookedTime());
        dto.setEstimatedTime(entity.getEstimatedTime());
        dto.setStoryPoints(entity.getStoryPoints());
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
        entity.setId(dto.getId() != null ? UUID.fromString(dto.getId()) : null);
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        entity.setProjectId(dto.getProjectId() != null ? UUID.fromString(dto.getProjectId()) : null);
        entity.setAssignTo(dto.getAssignTo() != null ? UUID.fromString(dto.getAssignTo()) : null);
        entity.setBookedTime(dto.getBookedTime());
        entity.setEstimatedTime(dto.getEstimatedTime());
        entity.setStoryPoints(dto.getStoryPoints());
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
        entity.setProjectId(dto.getProjectId() != null ? UUID.fromString(dto.getProjectId()) : null);
        entity.setAssignTo(dto.getAssignTo() != null ? UUID.fromString(dto.getAssignTo()) : null);
        entity.setEstimatedTime(dto.getEstimatedTime() != null ? Duration.parse(dto.getEstimatedTime()) : null);
        entity.setStoryPoints(dto.getStoryPoints());
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
        entity.setProjectId(dto.getProjectId() != null ? UUID.fromString(dto.getProjectId()) : null);
        entity.setAssignTo(dto.getAssignTo() != null ? UUID.fromString(dto.getAssignTo()) : null);
        entity.setEstimatedTime(dto.getEstimatedTime() != null ? Duration.parse(dto.getEstimatedTime()) : null);
        entity.setStoryPoints(dto.getStoryPoints());
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

