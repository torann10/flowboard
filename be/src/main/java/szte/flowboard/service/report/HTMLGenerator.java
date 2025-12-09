package szte.flowboard.service.report;

import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Service for generating HTML content from report data using Mustache templates.
 * Converts report DTOs into HTML strings that can be used for PDF generation.
 */
@Service
public class HTMLGenerator {

    /**
     * Generates HTML from an employee matrix data structure.
     * Transposes the matrix and renders it using the employee-matrix.mustache template.
     *
     * @param matrix a 2D array list representing the employee matrix (projects x users)
     * @return the generated HTML string
     * @throws IOException if the template file cannot be read
     */
    public String generateFromMatrix(ArrayList<ArrayList<String>> matrix) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/employee-matrix.mustache");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(resource.getInputStream()), "employee-matrix");
        Map<String, Object> context = new HashMap<>();

        int cols = matrix.size();
        int rows = matrix.getFirst().size();

        ArrayList<ArrayList<String>> transposed = new ArrayList<>();

        for (int r = 0; r < rows; r++) {
            ArrayList<String> newRow = new ArrayList<>();
            for (int c = 0; c < cols; c++) {
                newRow.add(matrix.get(c).get(r));
            }
            transposed.add(newRow);
        }

        List<Map<String, Object>> rowList = new ArrayList<>();

        for (var row : transposed) {
            Map<String, Object> rowMap = new HashMap<>();
            rowMap.put("cells", row);
            rowList.add(rowMap);
        }

        context.put("matrix", rowList);

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        return writer.toString();
    }

    /**
     * Generates HTML from a project activity report DTO.
     * Renders task activity data using the project-activity.mustache template.
     *
     * @param report the project activity report DTO containing task information
     * @return the generated HTML string
     * @throws IOException if the template file cannot be read
     */
    public String generateFromProjectActivity(szte.flowboard.dto.ProjectActivityReportDto report) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/project-activity.mustache");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(resource.getInputStream()), "project-activity");

        Map<String, Object> context = new HashMap<>();
        context.put("projectName", report.getName());
        context.put("start", report.getStart() != null ? report.getStart().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("end", report.getEnd() != null ? report.getEnd().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("createdAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")));
        context.put("taskCount", report.getLines().size() - 1);
        context.put("tasks", getProjectActivityLines(report));

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        return writer.toString();
    }

    /**
     * Generates HTML from a Certificate of Completion (COC) report DTO.
     * Renders COC data including customer, contractor, and line items using the coc-report.mustache template.
     *
     * @param report the COC report DTO containing billing information
     * @return the generated HTML string
     * @throws IOException if the template file cannot be read
     */
    public String generateFromCOC(szte.flowboard.dto.COCReportDto report) throws IOException {
        ClassPathResource resource = new ClassPathResource("templates/coc-report.mustache");
        MustacheFactory mf = new DefaultMustacheFactory();
        Mustache mustache = mf.compile(new InputStreamReader(resource.getInputStream()), "coc-report");

        Map<String, Object> context = new HashMap<>();
        context.put("customer", Map.of(
                "name", report.getCustomer() != null ? report.getCustomer().getName() : "",
                "address", report.getCustomer() != null ? report.getCustomer().getAddress() : ""
        ));
        context.put("contractor", Map.of(
                "name", report.getContractor() != null ? report.getContractor().getName() : "",
                "address", report.getContractor() != null ? report.getContractor().getAddress() : ""
        ));
        context.put("start", report.getStart() != null ? report.getStart().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("end", report.getEnd() != null ? report.getEnd().format(DateTimeFormatter.ofPattern("yyyy.MM.dd.")) : "");
        context.put("createdAt", report.getCreatedAt() != null ? report.getCreatedAt().format(DateTimeFormatter.ofPattern("yyyy.MM.dd. HH:mm")) : "");
        context.put("description", report.getDescription() != null ? report.getDescription() : "");
        context.put("lines", getCOCLines(report));

        StringWriter writer = new StringWriter();
        mustache.execute(writer, context).flush();
        return writer.toString();
    }

    private static List<Map<String, Object>> getProjectActivityLines(szte.flowboard.dto.ProjectActivityReportDto report) {
        List<Map<String, Object>> taskList = new ArrayList<>();

        for (var line : report.getLines()) {
            Map<String, Object> m = new HashMap<>();
            m.put("name", line.getName());
            m.put("spent", line.getSpentMinutes() != null ? String.format("%.2f óra", line.getSpentMinutes() / 60.0) : "");
            m.put("estimated", line.getEstimatedMinutes() != null ? String.format("%.2f óra", line.getEstimatedMinutes() / 60.0) : "");
            m.put("deviation", line.getDeviation() != null ? String.format("%.2f óra", line.getDeviation() / 60.0) : "");
            taskList.add(m);
        }
        return taskList;
    }

    private static List<Map<String, Object>> getCOCLines(szte.flowboard.dto.COCReportDto report) {
        List<Map<String, Object>> lines = new ArrayList<>();
        if (report.getLines() != null) {
            for (szte.flowboard.dto.COCReportLineItemDto line : report.getLines()) {
                Map<String, Object> lineMap = new HashMap<>();
                lineMap.put("name", line.getName() != null ? line.getName() : "");
                lineMap.put("quantity", line.getQuantity() != null ? String.format("%.2f", line.getQuantity()) : "");
                lineMap.put("unit", line.getUnit());
                lineMap.put("unitPrice", line.getUnitPrice() != null ? String.format("%,.0f Ft", line.getUnitPrice()) : "");
                lineMap.put("netPrice", line.getNetPrice() != null ? String.format("%,.0f Ft", line.getNetPrice()) : "");
                lineMap.put("vatPrice", line.getVatPrice() != null ? String.format("%,.0f Ft", line.getVatPrice()) : "");
                lineMap.put("grossPrice", line.getGrossPrice() != null ? String.format("%,.0f Ft", line.getGrossPrice()) : "");
                lines.add(lineMap);
            }
        }
        return lines;
    }
}

