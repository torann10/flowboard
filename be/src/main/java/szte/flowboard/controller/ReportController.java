package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.dto.ReportDto;
import szte.flowboard.dto.ReportCreateRequestDto;
import szte.flowboard.dto.ReportUpdateRequestDto;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.mapper.ReportMapper;
import szte.flowboard.service.ReportService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper reportMapper;

    @Operation(operationId = "createReport", summary = "Create report", description = "Creates a new report for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Report created successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ReportDto> create(@Valid @RequestBody ReportCreateRequestDto reportRequest, Authentication authentication) {
        ReportEntity report = reportMapper.toEntity(reportRequest);
        ReportEntity createdReport = reportService.create(report, authentication);
        ReportDto reportDto = reportMapper.toDto(createdReport);
        return ResponseEntity.status(HttpStatus.CREATED).body(reportDto);
    }

    @Operation(operationId = "getAllReports", summary = "Get all reports", description = "Retrieves all reports for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReportDto.class))))
    })
    @GetMapping
    public ResponseEntity<List<ReportDto>> findAll(Authentication authentication) {
        List<ReportEntity> reports = reportService.findAllByUser(authentication);
        List<ReportDto> reportDtos = reportMapper.toDtoList(reports);
        return ResponseEntity.ok(reportDtos);
    }

    @Operation(operationId = "getReportById", summary = "Get report by ID", description = "Retrieves a report by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report found successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportDto.class))),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> findById(@PathVariable UUID id, Authentication authentication) {
        Optional<ReportEntity> report = reportService.findByIdAndUser(id, authentication);
        return report.map(r -> ResponseEntity.ok(reportMapper.toDto(r)))
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(operationId = "updateReport", summary = "Update report", description = "Updates an existing report for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report updated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ReportDto.class))),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReportDto> update(@PathVariable UUID id, @Valid @RequestBody ReportUpdateRequestDto reportRequest, Authentication authentication) {
        if (!reportService.existsByIdAndUser(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        ReportEntity report = reportMapper.toEntity(reportRequest);
        report.setId(id);
        ReportEntity updatedReport = reportService.update(report, authentication);
        ReportDto reportDto = reportMapper.toDto(updatedReport);
        return ResponseEntity.ok(reportDto);
    }

    @Operation(operationId = "deleteReport", summary = "Delete report", description = "Deletes a report by its ID for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Report deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id, Authentication authentication) {
        if (!reportService.existsByIdAndUser(id, authentication)) {
            return ResponseEntity.notFound().build();
        }
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(operationId = "getReportCount", summary = "Get report count", description = "Retrieves the total number of reports for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Long.class)))
    })
    @GetMapping("/count")
    public ResponseEntity<Long> count(Authentication authentication) {
        long count = reportService.countByUser(authentication);
        return ResponseEntity.ok(count);
    }
}
