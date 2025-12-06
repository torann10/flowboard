package szte.flowboard.mapper;

import org.springframework.stereotype.Component;
import szte.flowboard.dto.ReportDto;
import szte.flowboard.entity.ReportEntity;

import java.util.List;

@Component
public class ReportMapper {

    public ReportDto toDto(ReportEntity entity) {
        if (entity == null) {
            return null;
        }

        ReportDto dto = new ReportDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setStart(entity.getStart());
        dto.setEnd(entity.getEnd());
        dto.setProjectName(entity.getProject() == null ? null : entity.getProject().getName());

        return dto;
    }

    public List<ReportDto> toDto(List<ReportEntity> reports) {
        return reports.stream().map(this::toDto).toList();
    }

}
