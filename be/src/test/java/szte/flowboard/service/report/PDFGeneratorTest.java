package szte.flowboard.service.report;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class PDFGeneratorTest {

    private PDFGenerator pdfGenerator;

    @BeforeEach
    void setUp() {
        pdfGenerator = new PDFGenerator();
    }

    @Test
    void testGeneratePdf_SimpleHtml_Success() {
        // Given
        String html = "<html><body><h1>Test PDF</h1><p>This is a test PDF document.</p></body></html>";

        // When
        byte[] result = pdfGenerator.generatePdf(html);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGeneratePdf_EmptyHtml_Success() {
        // Given
        String html = "<html><body></body></html>";

        // When
        byte[] result = pdfGenerator.generatePdf(html);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGeneratePdf_ComplexHtml_Success() {
        // Given
        String html = """
            <html>
                <head>
                    <style>
                        table { border-collapse: collapse; width: 100%; }
                        th, td { border: 1px solid black; padding: 8px; }
                    </style>
                </head>
                <body>
                    <h1>Test Report</h1>
                    <table>
                        <tr>
                            <th>Name</th>
                            <th>Value</th>
                        </tr>
                        <tr>
                            <td>Item 1</td>
                            <td>100</td>
                        </tr>
                        <tr>
                            <td>Item 2</td>
                            <td>200</td>
                        </tr>
                    </table>
                </body>
            </html>
            """;

        // When
        byte[] result = pdfGenerator.generatePdf(html);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGeneratePdf_WithUnicodeCharacters_Success() {
        // Given
        String html = "<html><body><h1>Test PDF with Unicode</h1><p>Árvíztűrő tükörfúrógép</p></body></html>";

        // When
        byte[] result = pdfGenerator.generatePdf(html);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGeneratePdf_WithSpecialCharacters_Success() {
        // Given
        String html = "<html><body><h1>Special Characters</h1><p>&lt;test&gt; &amp; &quot;quotes&quot;</p></body></html>";

        // When
        byte[] result = pdfGenerator.generatePdf(html);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGeneratePdf_MultiplePages_Success() {
        // Given
        StringBuilder htmlBuilder = new StringBuilder();
        htmlBuilder.append("<html><body>");
        for (int i = 0; i < 50; i++) {
            htmlBuilder.append("<p>This is paragraph ").append(i).append(".</p>");
        }
        htmlBuilder.append("</body></html>");
        String html = htmlBuilder.toString();

        // When
        byte[] result = pdfGenerator.generatePdf(html);

        // Then
        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void testGeneratePdf_InvalidHtml_ThrowsException() {
        // Given
        // PDFGenerator throws RuntimeException when PDF generation fails
        String invalidHtml = "<html><body><unclosed-tag></body></html>";

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            pdfGenerator.generatePdf(invalidHtml);
        });
    }
}

