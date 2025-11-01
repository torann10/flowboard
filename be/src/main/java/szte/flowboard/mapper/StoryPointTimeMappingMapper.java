package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.StoryPointTimeMappingDto;
import szte.flowboard.entity.StoryPointTimeMappingEntity;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class StoryPointTimeMappingMapper implements EntityMapper<StoryPointTimeMappingEntity, StoryPointTimeMappingDto> {

    @Override
    public StoryPointTimeMappingDto toDto(StoryPointTimeMappingEntity entity) {
        if (entity == null) {
            return null;
        }

        StoryPointTimeMappingDto dto = new StoryPointTimeMappingDto();
        dto.setId(entity.getId());
        dto.setProjectId(entity.getProject().getId());
        dto.setStoryPoints(entity.getStoryPoints());
        dto.setTimeValue(entity.getTimeValue());

        return dto;
    }

    @Override
    public StoryPointTimeMappingEntity toEntity(StoryPointTimeMappingDto dto) {
        if (dto == null) {
            return null;
        }

        StoryPointTimeMappingEntity entity = new StoryPointTimeMappingEntity();
        entity.setId(dto.getId());
        entity.setStoryPoints(dto.getStoryPoints());
        entity.setTimeValue(dto.getTimeValue());

        return entity;
    }

    @Override
    public List<StoryPointTimeMappingDto> toDtoList(List<StoryPointTimeMappingEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<StoryPointTimeMappingEntity> toEntityList(List<StoryPointTimeMappingDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

