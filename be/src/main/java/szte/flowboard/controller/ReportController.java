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
import szte.flowboard.dto.request.CreateCOCReportRequestDto;
import szte.flowboard.dto.request.CreateEmployeeMatrixReportRequestDto;
import szte.flowboard.dto.request.CreateProjectActivityReportRequestDto;
import szte.flowboard.dto.response.DownloadReportDto;
import szte.flowboard.mapper.ReportMapper;
import szte.flowboard.service.ReportService;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * REST controller for managing reports.
 * Provides endpoints for creating various types of reports (COC, Employee Matrix, Project Activity),
 * listing reports, downloading reports, renaming, and deleting reports.
 * All operations are scoped to the authenticated user's accessible reports.
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;
    private final ReportMapper reportMapper;

    /**
     * Lists all reports accessible by the current user.
     *
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing a list of report DTOs with HTTP status 200
     */
    @Operation(operationId = "listReportsForUser", summary = "Lists the reports for the user", description = "Lists the available reports for the user")
    @ApiResponse(responseCode = "200", description = "Reports were successfully retrieved", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ReportDto.class))))
    @GetMapping
    public ResponseEntity<List<ReportDto>> list(Authentication authentication) {
        var reports = reportService.findAllByUser(authentication);

        return ResponseEntity.ok(reportMapper.toDto(reports));
    }

    /**
     * Creates a Certificate of Completion (COC) report PDF for a project.
     * The report can be generated for time-based or story-point-based projects.
     *
     * @param reportRequest the COC report creation request containing project and date range details
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the report ID with HTTP status 200, or 400 if input is invalid
     * @throws IOException if an I/O error occurs during report generation
     */
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

    /**
     * Creates an employee matrix report PDF showing time logged by employees across projects.
     *
     * @param reportRequest the employee matrix report creation request containing date range details
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the report ID with HTTP status 200, or 400 if input is invalid
     * @throws IOException if an I/O error occurs during report generation
     */
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

    /**
     * Creates a project activity report PDF showing task activity for a specific project.
     *
     * @param reportRequest the project activity report creation request containing project and date range details
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the report ID with HTTP status 200, or 400 if input is invalid
     * @throws IOException if an I/O error occurs during report generation
     */
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

    /**
     * Retrieves a short-lived presigned download URL for a report from S3.
     *
     * @param reportId the unique identifier of the report
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity containing the download URL DTO with HTTP status 200, or 400 if invalid
     */
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

    /**
     * Renames a report if the current user has access to it.
     *
     * @param reportId the unique identifier of the report to rename
     * @param name the new name for the report
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity with HTTP status 204 if renamed successfully, or 400 if invalid
     */
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

    /**
     * Deletes a report and its associated file from S3 if the current user has access to it.
     *
     * @param reportId the unique identifier of the report to delete
     * @param authentication the authentication object containing the current user's information
     * @return ResponseEntity with HTTP status 204 if deleted successfully, or 400 if invalid
     */
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
