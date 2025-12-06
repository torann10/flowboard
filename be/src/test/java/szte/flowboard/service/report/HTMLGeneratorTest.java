package szte.flowboard.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import szte.flowboard.dto.COCReportDto;
import szte.flowboard.dto.COCReportLineItemDto;
import szte.flowboard.dto.ProjectActivityReportDto;
import szte.flowboard.dto.ProjectActivityReportLineItemDto;
import szte.flowboard.entity.CompanyEntity;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HTMLGeneratorTest {

    @InjectMocks
    private HTMLGenerator htmlGenerator;

    @BeforeEach
    void setUp() {
        // HTMLGenerator uses ClassPathResource which requires templates to exist
        // If templates don't exist in test resources, tests will fail
        // This is expected behavior - templates should be in src/main/resources/templates/
    }

    @Test
    void testGenerateFromMatrix_Success() throws IOException {
        // Given
        ArrayList<ArrayList<String>> matrix = new ArrayList<>();
        ArrayList<String> row1 = new ArrayList<>();
        row1.add("Név");
        row1.add("John Doe");
        row1.add("Jane Smith");
        matrix.add(row1);

        ArrayList<String> row2 = new ArrayList<>();
        row2.add("Project 1");
        row2.add("5 óra");
        row2.add("3 óra");
        matrix.add(row2);

        // When
        String result = htmlGenerator.generateFromMatrix(matrix);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGenerateFromMatrix_EmptyMatrix() throws IOException {
        // Given
        ArrayList<ArrayList<String>> matrix = new ArrayList<>();
        ArrayList<String> row = new ArrayList<>();
        matrix.add(row);

        // When
        String result = htmlGenerator.generateFromMatrix(matrix);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromMatrix_SingleColumn() throws IOException {
        // Given
        ArrayList<ArrayList<String>> matrix = new ArrayList<>();
        ArrayList<String> row = new ArrayList<>();
        row.add("Név");
        row.add("John Doe");
        matrix.add(row);

        // When
        String result = htmlGenerator.generateFromMatrix(matrix);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromProjectActivity_Success() throws IOException {
        // Given
        ProjectActivityReportLineItemDto line1 = new ProjectActivityReportLineItemDto(
            "Task 1", 480L, 600L, -120L);
        ProjectActivityReportLineItemDto line2 = new ProjectActivityReportLineItemDto(
            "Task 2", 720L, 600L, 120L);
        ProjectActivityReportLineItemDto summary = new ProjectActivityReportLineItemDto(
            "Összesen", 1200L, 1200L, 0L);

        ProjectActivityReportDto report = new ProjectActivityReportDto();
        report.setName("Test Project");
        report.setStart(LocalDate.now().minusDays(7));
        report.setEnd(LocalDate.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setLines(List.of(line1, line2, summary));

        // When
        String result = htmlGenerator.generateFromProjectActivity(report);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGenerateFromProjectActivity_WithNullDates() throws IOException {
        // Given
        ProjectActivityReportLineItemDto line = new ProjectActivityReportLineItemDto(
            "Task 1", 480L, 600L, -120L);

        ProjectActivityReportDto report = new ProjectActivityReportDto();
        report.setName("Test Project");
        report.setStart(null);
        report.setEnd(null);
        report.setCreatedAt(LocalDateTime.now());
        report.setLines(List.of(line));

        // When
        String result = htmlGenerator.generateFromProjectActivity(report);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromProjectActivity_WithNullMinutes() throws IOException {
        // Given
        ProjectActivityReportLineItemDto line = new ProjectActivityReportLineItemDto(
            "Task 1", null, null, null);

        ProjectActivityReportDto report = new ProjectActivityReportDto();
        report.setName("Test Project");
        report.setStart(LocalDate.now().minusDays(7));
        report.setEnd(LocalDate.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setLines(List.of(line));

        // When
        String result = htmlGenerator.generateFromProjectActivity(report);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromProjectActivity_EmptyLines() throws IOException {
        // Given
        ProjectActivityReportDto report = new ProjectActivityReportDto();
        report.setName("Test Project");
        report.setStart(LocalDate.now().minusDays(7));
        report.setEnd(LocalDate.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setLines(Collections.emptyList());

        // When
        String result = htmlGenerator.generateFromProjectActivity(report);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromCOC_Success() throws IOException {
        // Given
        CompanyEntity customer = new CompanyEntity();
        customer.setName("Test Customer");
        customer.setAddress("Test Customer Address");

        CompanyEntity contractor = new CompanyEntity();
        contractor.setName("Test Contractor");
        contractor.setAddress("Test Contractor Address");

        COCReportLineItemDto line1 = new COCReportLineItemDto();
        line1.setName("John Doe");
        line1.setQuantity(8.0);
        line1.setUnit("óra");
        line1.setUnitPrice(100.0);
        line1.setNetPrice(800.0);
        line1.setVatPrice(216.0);
        line1.setGrossPrice(1016.0);

        COCReportLineItemDto summary = new COCReportLineItemDto();
        summary.setName("Összesen");
        summary.setNetPrice(800.0);
        summary.setVatPrice(216.0);
        summary.setGrossPrice(1016.0);

        COCReportDto report = new COCReportDto();
        report.setStart(LocalDate.now().minusDays(7));
        report.setEnd(LocalDate.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setCustomer(customer);
        report.setContractor(contractor);
        report.setDescription("Test Description");
        report.setLines(List.of(line1, summary));

        // When
        String result = htmlGenerator.generateFromCOC(report);

        // Then
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testGenerateFromCOC_WithNullValues() throws IOException {
        // Given
        COCReportDto report = new COCReportDto();
        report.setStart(null);
        report.setEnd(null);
        report.setCreatedAt(null);
        report.setCustomer(null);
        report.setContractor(null);
        report.setDescription(null);
        report.setLines(null);

        // When
        String result = htmlGenerator.generateFromCOC(report);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromCOC_WithEmptyLines() throws IOException {
        // Given
        CompanyEntity customer = new CompanyEntity();
        customer.setName("Test Customer");
        customer.setAddress("Test Customer Address");

        CompanyEntity contractor = new CompanyEntity();
        contractor.setName("Test Contractor");
        contractor.setAddress("Test Contractor Address");

        COCReportDto report = new COCReportDto();
        report.setStart(LocalDate.now().minusDays(7));
        report.setEnd(LocalDate.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setCustomer(customer);
        report.setContractor(contractor);
        report.setDescription("Test Description");
        report.setLines(Collections.emptyList());

        // When
        String result = htmlGenerator.generateFromCOC(report);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromCOC_WithNullLineValues() throws IOException {
        // Given
        CompanyEntity customer = new CompanyEntity();
        customer.setName("Test Customer");
        customer.setAddress("Test Customer Address");

        CompanyEntity contractor = new CompanyEntity();
        contractor.setName("Test Contractor");
        contractor.setAddress("Test Contractor Address");

        COCReportLineItemDto line = new COCReportLineItemDto();
        line.setName(null);
        line.setQuantity(null);
        line.setUnit(null);
        line.setUnitPrice(null);
        line.setNetPrice(null);
        line.setVatPrice(null);
        line.setGrossPrice(null);

        COCReportDto report = new COCReportDto();
        report.setStart(LocalDate.now().minusDays(7));
        report.setEnd(LocalDate.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setCustomer(customer);
        report.setContractor(contractor);
        report.setDescription("Test Description");
        report.setLines(List.of(line));

        // When
        String result = htmlGenerator.generateFromCOC(report);

        // Then
        assertNotNull(result);
    }

    @Test
    void testGenerateFromCOC_WithZeroPrices() throws IOException {
        // Given
        CompanyEntity customer = new CompanyEntity();
        customer.setName("Test Customer");
        customer.setAddress("Test Customer Address");

        CompanyEntity contractor = new CompanyEntity();
        contractor.setName("Test Contractor");
        contractor.setAddress("Test Contractor Address");

        COCReportLineItemDto line = new COCReportLineItemDto();
        line.setName("Test Item");
        line.setQuantity(0.0);
        line.setUnit("óra");
        line.setUnitPrice(0.0);
        line.setNetPrice(0.0);
        line.setVatPrice(0.0);
        line.setGrossPrice(0.0);

        COCReportDto report = new COCReportDto();
        report.setStart(LocalDate.now().minusDays(7));
        report.setEnd(LocalDate.now());
        report.setCreatedAt(LocalDateTime.now());
        report.setCustomer(customer);
        report.setContractor(contractor);
        report.setDescription("Test Description");
        report.setLines(List.of(line));

        // When
        String result = htmlGenerator.generateFromCOC(report);

        // Then
        assertNotNull(result);
    }
}

