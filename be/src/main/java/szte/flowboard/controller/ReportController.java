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
import szte.flowboard.dto.ReportCreateRequestDto;
import szte.flowboard.service.ReportService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@RestController
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    @Operation(operationId = "createReport", summary = "Create report", description = "Creates a new report PDF for the current user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Report PDF generated successfully", content = @Content(mediaType = "application/pdf")),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    @PostMapping(produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> create(@Valid @RequestBody ReportCreateRequestDto reportRequest, Authentication authentication) {
        byte[] pdfBytes = reportService.create(reportRequest, authentication);
        
        if (pdfBytes == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        String filename = String.format("teljesitesi_igazolas_%s.pdf",
            LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentLength(pdfBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
