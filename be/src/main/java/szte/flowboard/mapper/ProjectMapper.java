package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.ProjectDto;
import szte.flowboard.dto.ProjectCreateRequestDto;
import szte.flowboard.dto.ProjectUpdateRequestDto;
import szte.flowboard.entity.ProjectEntity;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class ProjectMapper implements EntityMapper<ProjectEntity, ProjectDto> {

    private final StoryPointTimeMappingMapper storyPointTimeMappingMapper;

    private final CompanyMapper companyMapper;

    public ProjectMapper(StoryPointTimeMappingMapper storyPointTimeMappingMapper, CompanyMapper companyMapper) {
        this.storyPointTimeMappingMapper = storyPointTimeMappingMapper;
        this.companyMapper = companyMapper;
    }

    @Override
    public ProjectDto toDto(ProjectEntity entity) {
        if (entity == null) {
            return null;
        }

        ProjectDto dto = new ProjectDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setStatus(entity.getStatus());
        dto.setType(entity.getType());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreateAt(entity.getCreatedAt());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedAt(entity.getLastModifiedAt());
        dto.setCustomer(companyMapper.toDto(entity.getCustomer()));
        dto.setContractor(companyMapper.toDto(entity.getContractor()));

        if (entity.getStoryPointTimeMappings() != null) {
            dto.setStoryPointTimeMappings(
                storyPointTimeMappingMapper.toDtoList(entity.getStoryPointTimeMappings())
            );
        }

        return dto;
    }

    @Override
    public ProjectEntity toEntity(ProjectDto dto) {
        if (dto == null) {
            return null;
        }

        ProjectEntity entity = new ProjectEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        entity.setType(dto.getType());
        entity.setLastModifiedBy(dto.getLastModifiedBy());
        entity.setLastModifiedAt(dto.getLastModifiedAt());
        entity.setCustomer(companyMapper.toEntity(dto.getCustomer()));
        entity.setContractor(companyMapper.toEntity(dto.getContractor()));

        if (dto.getStoryPointTimeMappings() != null) {
            entity.setStoryPointTimeMappings(
                storyPointTimeMappingMapper.toEntityList(dto.getStoryPointTimeMappings())
            );

            entity.getStoryPointTimeMappings().forEach(storyPointTimeMapping -> {
                storyPointTimeMapping.setProject(entity);
            });
        }

        return entity;
    }

    public ProjectEntity toEntity(ProjectCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ProjectEntity entity = new ProjectEntity();
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        entity.setType(dto.getType());
        entity.setCustomer(companyMapper.toEntity(dto.getCustomer()));
        entity.setContractor(companyMapper.toEntity(dto.getContractor()));

        if (dto.getStoryPointTimeMappings() != null) {
            entity.setStoryPointTimeMappings(
                storyPointTimeMappingMapper.toEntityList(dto.getStoryPointTimeMappings())
            );

            entity.getStoryPointTimeMappings().forEach(storyPointTimeMapping -> {
                storyPointTimeMapping.setProject(entity);
            });
        }

        return entity;
    }

    public ProjectEntity toEntity(ProjectUpdateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        ProjectEntity entity = new ProjectEntity();
        entity.setName(dto.getName());
        entity.setStatus(dto.getStatus());
        entity.setType(dto.getType());
        entity.setCustomer(companyMapper.toEntity(dto.getCustomer()));
        entity.setContractor(companyMapper.toEntity(dto.getContractor()));

        if (dto.getStoryPointTimeMappings() != null) {
            entity.setStoryPointTimeMappings(
                storyPointTimeMappingMapper.toEntityList(dto.getStoryPointTimeMappings())
            );

            entity.getStoryPointTimeMappings().forEach(storyPointTimeMapping -> {
                storyPointTimeMapping.setProject(entity);
            });
        }

        return entity;
    }

    @Override
    public List<ProjectDto> toDtoList(List<ProjectEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ProjectEntity> toEntityList(List<ProjectDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

