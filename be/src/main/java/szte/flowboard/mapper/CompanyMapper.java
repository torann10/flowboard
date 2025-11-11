package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.CompanyDto;
import szte.flowboard.entity.CompanyEntity;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CompanyMapper implements EntityMapper<CompanyEntity, CompanyDto> {

    @Override
    public CompanyDto toDto(CompanyEntity entity) {
        if (entity == null) {
            return null;
        }

        CompanyDto dto = new CompanyDto();
        dto.setName(entity.getName());
        dto.setAddress(entity.getAddress());

        return dto;
    }

    @Override
    public CompanyEntity toEntity(CompanyDto dto) {
        if(dto == null) {
            return null;
        }

        CompanyEntity entity = new CompanyEntity();
        entity.setName(dto.getName());
        entity.setAddress(dto.getAddress());

        return entity;
    }

    @Override
    public List<CompanyDto> toDtoList(List<CompanyEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CompanyEntity> toEntityList(List<CompanyDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}
