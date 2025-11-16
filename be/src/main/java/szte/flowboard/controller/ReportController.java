package szte.flowboard.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import szte.flowboard.dto.CreateCOCReportRequestDto;
import szte.flowboard.dto.CreateEmployeeMatrixReportRequestDto;
import szte.flowboard.service.ReportService;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(operationId = "createCocReport", summary = "Create COC report", description = "Creates a new report PDF for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report PDF generated successfully", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(path = "coc", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> create(@Valid @RequestBody CreateCOCReportRequestDto reportRequest, Authentication authentication) throws IOException {
        byte[] pdfBytes = reportService.createCOC(reportRequest, authentication);

        String filename = String.format("teljesitesi_igazolas_%s.pdf",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        return formatResponse(pdfBytes, filename);
    }

    @Operation(operationId = "createEmployeeMatrixReport", summary = "Create employee matrix report", description = "Creates a new report PDF for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report PDF generated successfully", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(path = "employee-matrix", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> create(@Valid @RequestBody CreateEmployeeMatrixReportRequestDto reportRequest, Authentication authentication) throws IOException {
        byte[] pdfBytes = reportService.createEmployeeMatrix(reportRequest, authentication);

        String filename = String.format("munkavallaloi-matrix_%s.pdf",
                LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));

        return formatResponse(pdfBytes, filename);
    }

    private ResponseEntity<byte[]> formatResponse(byte[] pdfBytes, String name) {
        if (pdfBytes == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", name);
        headers.setContentLength(pdfBytes.length);

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
