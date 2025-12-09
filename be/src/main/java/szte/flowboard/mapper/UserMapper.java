package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.UserDto;
import szte.flowboard.dto.request.UserCreateRequestDto;
import szte.flowboard.dto.request.UserUpdateRequestDto;
import szte.flowboard.entity.UserEntity;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper implements EntityMapper<UserEntity, UserDto> {

    @Override
    public UserDto toDto(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        UserDto dto = new UserDto();
        dto.setId(entity.getId());
        dto.setFirstName(entity.getFirstName());
        dto.setLastName(entity.getLastName());
        dto.setEmailAddress(entity.getEmailAddress());
        dto.setKeycloakId(entity.getKeycloakId());
        dto.setCreatedBy(entity.getCreatedBy());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setLastModifiedBy(entity.getLastModifiedBy());
        dto.setLastModifiedAt(entity.getLastModifiedAt());

        return dto;
    }

    @Override
    public UserEntity toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setId(dto.getId());
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmailAddress(dto.getEmailAddress());
        entity.setKeycloakId(dto.getKeycloakId());
        entity.setLastModifiedBy(dto.getLastModifiedBy());
        entity.setLastModifiedAt(dto.getLastModifiedAt());

        return entity;
    }

    public UserEntity toEntity(UserCreateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmailAddress(dto.getEmailAddress());
        entity.setKeycloakId(dto.getKeycloakId());

        return entity;
    }

    public UserEntity toEntity(UserUpdateRequestDto dto) {
        if (dto == null) {
            return null;
        }

        UserEntity entity = new UserEntity();
        entity.setFirstName(dto.getFirstName());
        entity.setLastName(dto.getLastName());
        entity.setEmailAddress(dto.getEmailAddress());

        return entity;
    }

    @Override
    public List<UserDto> toDtoList(List<UserEntity> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<UserEntity> toEntityList(List<UserDto> dtos) {
        if (dtos == null) {
            return null;
        }
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
}

