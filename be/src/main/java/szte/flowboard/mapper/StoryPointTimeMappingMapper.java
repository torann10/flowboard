package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.StoryPointTimeMappingDto;
import szte.flowboard.entity.StoryPointTimeMapping;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class StoryPointTimeMappingMapper implements EntityMapper<StoryPointTimeMapping, StoryPointTimeMappingDto> {

    @Override
    public StoryPointTimeMappingDto toDto(StoryPointTimeMapping entity) {
        if (entity == null) {
            return null;
        }

        StoryPointTimeMappingDto dto = new StoryPointTimeMappingDto();
        dto.setId(entity.getId() != null ? entity.getId().toString() : null);
        dto.setProjectId(entity.getProject() != null ? entity.getProject().getId().toString() : null);
        dto.setStoryPoints(entity.getStoryPoints());
        dto.setTimeValue(entity.getTimeValue());

        return dto;
    }

    @Override
    public StoryPointTimeMapping toEntity(StoryPointTimeMappingDto dto) {
        if (dto == null) {
            return null;
        }

        StoryPointTimeMapping entity = new StoryPointTimeMapping();
        entity.setId(dto.getId() != null ? UUID.fromString(dto.getId()) : null);
        entity.setStoryPoints(dto.getStoryPoints());
        entity.setTimeValue(dto.getTimeValue());

        return entity;
    }

    @Override
    public List<StoryPointTimeMappingDto> toDtoList(List<StoryPointTimeMapping> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoryPointTimeMapping> toEntityList(List<StoryPointTimeMappingDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

