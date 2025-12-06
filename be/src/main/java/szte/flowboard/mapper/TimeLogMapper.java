package szte.flowboard.mapper;

import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Component;
import szte.flowboard.dto.TimeLogDto;
import szte.flowboard.dto.request.TimeLogUpdateRequestDto;
import szte.flowboard.entity.TaskEntity;
import szte.flowboard.entity.TimeLogEntity;
import szte.flowboard.entity.UserEntity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TimeLogMapper implements EntityMapper<TimeLogEntity, TimeLogDto> {

    private final EntityManager entityManager;

    public TimeLogMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public TimeLogDto toDto(TimeLogEntity entity) {
        if (entity == null) {
            return null;
        }

        TimeLogDto dto = new TimeLogDto();
        dto.setId(entity.getId());
        dto.setTaskId(entity.getTask().getId());
        dto.setUserId(entity.getUser().getId());
        dto.setLoggedTime(entity.getLoggedTime());
        dto.setLogDate(entity.getLogDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedAt(entity.getLastModifiedAt());
        dto.setBillable(entity.getIsBillable());

        return dto;
    }

    @Override
    public TimeLogEntity toEntity(TimeLogDto dto) {
        if (dto == null) {
            return null;
        }

        TimeLogEntity entity = new TimeLogEntity();
        entity.setId(dto.getId());
        entity.setTask(entityManager.getReference(TaskEntity.class, dto.getTaskId()));
        entity.setUser(entityManager.getReference(UserEntity.class, dto.getUserId()));
        entity.setLoggedTime(dto.getLoggedTime());
        entity.setLogDate(dto.getLogDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setLastModifiedBy(dto.getLastModifiedBy());
        entity.setLastModifiedAt(dto.getLastModifiedAt());
        entity.setIsBillable(dto.isBillable());

        return entity;
    }

    public TimeLogEntity toEntity(TimeLogUpdateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        TimeLogEntity entity = new TimeLogEntity();
        entity.setTask(entityManager.getReference(TaskEntity.class, dto.getTaskId()));
        entity.setLoggedTime(dto.getLoggedTime() != null ? dto.getLoggedTime() : null);
        entity.setLogDate(dto.getLogDate());
        entity.setIsBillable(dto.isBillable());

        return entity;
    }

    @Override
    public List<TimeLogDto> toDtoList(List<TimeLogEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TimeLogEntity> toEntityList(List<TimeLogDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

