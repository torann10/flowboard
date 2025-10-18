package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.entity.ReportEntity;
import szte.flowboard.service.ReportService;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(summary = "Create report", description = "Creates a new report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Report created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping
    public ResponseEntity<ReportEntity> create(@RequestBody ReportEntity report) {
        ReportEntity createdReport = reportService.create(report);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdReport);
    }

    @Operation(summary = "Get all reports", description = "Retrieves all reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reports retrieved successfully")
    })
    @GetMapping
    public ResponseEntity<List<ReportEntity>> findAll() {
        List<ReportEntity> reports = reportService.findAll();
        return ResponseEntity.ok(reports);
    }

    @Operation(summary = "Get report by ID", description = "Retrieves a report by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report found successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReportEntity> findById(@PathVariable UUID id) {
        Optional<ReportEntity> report = reportService.findById(id);
        return report.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update report", description = "Updates an existing report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report updated successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ReportEntity> update(@PathVariable UUID id, @RequestBody ReportEntity report) {
        if (!reportService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        report.setId(id);
        ReportEntity updatedReport = reportService.update(report);
        return ResponseEntity.ok(updatedReport);
    }

    @Operation(summary = "Delete report", description = "Deletes a report by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Report deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Report not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        if (!reportService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        reportService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get report count", description = "Retrieves the total number of reports")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Count retrieved successfully")
    })
    @GetMapping("/count")
    public ResponseEntity<Long> count() {
        long count = reportService.count();
        return ResponseEntity.ok(count);
    }
}
