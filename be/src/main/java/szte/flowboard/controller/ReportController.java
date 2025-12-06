package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import szte.flowboard.dto.*;
import szte.flowboard.mapper.ReportMapper;
import szte.flowboard.service.ReportService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper reportMapper;

    @Operation(operationId = "listReportsForUser", summary = "Lists the reports for the user", description = "Lists the available reports for the user")
    @ApiResponse(responseCode = "200", description = "Reports were successfully retrieved", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReportDto.class))))
    @GetMapping
    public ResponseEntity<List<ReportDto>> list(Authentication authentication) {
        var reports = reportService.findAllByUser(authentication);

        return ResponseEntity.ok(reportMapper.toDto(reports));
    }

    @Operation(operationId = "createCocReport", summary = "Create COC report", description = "Creates a new report PDF for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report PDF generated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(path = "coc")
    public ResponseEntity<UUID> create(@Valid @RequestBody CreateCOCReportRequestDto reportRequest, Authentication authentication) throws IOException {
        var id = reportService.createCOC(reportRequest, authentication);

        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body(id);
    }

    @Operation(operationId = "createEmployeeMatrixReport", summary = "Create employee matrix report", description = "Creates a new report PDF for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report PDF generated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(path = "employee-matrix")
    public ResponseEntity<UUID> create(@Valid @RequestBody CreateEmployeeMatrixReportRequestDto reportRequest, Authentication authentication) throws IOException {
        var id = reportService.createEmployeeMatrix(reportRequest, authentication);

        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body(id);
    }

    @Operation(operationId = "createProjectActivityReport", summary = "Create project activity report", description = "Creates a new report PDF for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report PDF generated successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UUID.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(path = "project-activity")
    public ResponseEntity<UUID> create(@Valid @RequestBody CreateProjectActivityReportRequestDto reportRequest, Authentication authentication) throws IOException {
        var id = reportService.createProjectActivityReport(reportRequest, authentication);

        if (id == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok().body(id);
    }

    @Operation(operationId = "getReportDownloadUrl", summary = "Retrieve a report download url", description = "Retrieves a short lived download url for the report")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "The download url was successfully retrieved", content = @Content(mediaType = "application/json", schema = @Schema(implementation = DownloadReportDto.class))),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    @GetMapping("{reportId}/download")
    public ResponseEntity<DownloadReportDto> getDownloadUrl(@PathVariable UUID reportId, Authentication authentication) {
        var url = reportService.getDownloadUrl(reportId, authentication);

        return url == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok().body(new DownloadReportDto(url));
    }

    @Operation(operationId = "renameReport", summary = "Renames a report", description = "Renames a report with the given unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The report was successfully renamed"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    @PatchMapping("{reportId}/rename/{name}")
    public ResponseEntity<Void> rename(@PathVariable UUID reportId, @PathVariable String name, Authentication authentication) {
        var success = reportService.renameReport(reportId, name, authentication);

        return success ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }

    @Operation(operationId = "deleteReport", summary = "Delete a report", description = "Deletes a report with the given unique identifier")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "The report was successfully deleted"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
    })
    @GetMapping("{reportId}")
    public ResponseEntity<Void> delete(@PathVariable UUID reportId, Authentication authentication) {
        var success = reportService.deleteReport(reportId, authentication);

        return success ? ResponseEntity.noContent().build() : ResponseEntity.badRequest().build();
    }

}
