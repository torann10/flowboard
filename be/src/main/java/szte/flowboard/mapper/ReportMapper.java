package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.ReportDto;
import szte.flowboard.dto.ReportCreateRequestDto;
import szte.flowboard.dto.ReportUpdateRequestDto;
import szte.flowboard.entity.ReportEntity;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ReportMapper implements EntityMapper<ReportEntity, ReportDto> {

    @Override
    public ReportDto toDto(ReportEntity entity) {
        if (entity == null) {
            return null;
        }

        ReportDto dto = new ReportDto();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setProjectId(entity.getProjectId() != null ? entity.getProjectId().toString() : null);
        dto.setUserId(entity.getUserId() != null ? entity.getUserId().toString() : null);
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedAt(entity.getLastModifiedAt());

        return dto;
    }

    @Override
    public ReportEntity toEntity(ReportDto dto) {
        if (dto == null) {
            return null;
        }

        ReportEntity entity = new ReportEntity();
        entity.setId(dto.getId() != null ? UUID.fromString(dto.getId()) : null);
        entity.setProjectId(dto.getProjectId() != null ? UUID.fromString(dto.getProjectId()) : null);
        entity.setUserId(dto.getUserId() != null ? UUID.fromString(dto.getUserId()) : null);
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(dto.getCreatedAt());
        entity.setLastModifiedBy(dto.getLastModifiedBy());
        entity.setLastModifiedAt(dto.getLastModifiedAt());

        return entity;
    }

    public ReportEntity toEntity(ReportCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ReportEntity entity = new ReportEntity();
        entity.setProjectId(dto.getProjectId() != null ? UUID.fromString(dto.getProjectId()) : null);
        entity.setUserId(dto.getUserId() != null ? UUID.fromString(dto.getUserId()) : null);

        return entity;
    }

    public ReportEntity toEntity(ReportUpdateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ReportEntity entity = new ReportEntity();
        entity.setProjectId(dto.getProjectId() != null ? UUID.fromString(dto.getProjectId()) : null);
        entity.setUserId(dto.getUserId() != null ? UUID.fromString(dto.getUserId()) : null);

        return entity;
    }

    @Override
    public List<ReportDto> toDtoList(List<ReportEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReportEntity> toEntityList(List<ReportDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

