package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.TimeLogDto;
import szte.flowboard.dto.TimeLogUpdateRequestDto;
import szte.flowboard.entity.TimeLogEntity;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class TimeLogMapper implements EntityMapper<TimeLogEntity, TimeLogDto> {

    @Override
    public TimeLogDto toDto(TimeLogEntity entity) {
        if (entity == null) {
            return null;
        }

        TimeLogDto dto = new TimeLogDto();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setTaskId(entity.getTaskId() != null ? entity.getTaskId().toString() : null);
        dto.setUserId(entity.getUserId() != null ? entity.getUserId().toString() : null);
        dto.setLoggedTime(entity.getLoggedTime());
        dto.setLogDate(entity.getLogDate());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreateAt(entity.getCreatedAt());
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
        entity.setId(dto.getId() != null ? UUID.fromString(dto.getId()) : null);
        entity.setTaskId(dto.getTaskId() != null ? UUID.fromString(dto.getTaskId()) : null);
        entity.setUserId(dto.getUserId() != null ? UUID.fromString(dto.getUserId()) : null);
        entity.setLoggedTime(dto.getLoggedTime());
        entity.setLogDate(dto.getLogDate());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreateAt());
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
        entity.setTaskId(dto.getTaskId() != null ? UUID.fromString(dto.getTaskId()) : null);
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

