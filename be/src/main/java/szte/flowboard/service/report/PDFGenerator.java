package szte.flowboard.service.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Service for generating PDF documents from HTML content.
 * Uses OpenHTMLToPDF library to convert HTML strings to PDF byte arrays.
 * Includes PT Mono font for proper character rendering.
 */
@Service
public class PDFGenerator {

    /**
     * Generates a PDF document from HTML content.
     * Loads the PT Mono font from classpath resources and embeds it in the PDF.
     *
     * @param html the HTML content to convert to PDF
     * @return the PDF as a byte array
     * @throws RuntimeException if PDF generation fails
     */
    public byte[] generatePdf(String html) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            PdfRendererBuilder builder = new PdfRendererBuilder();
            builder.withHtmlContent(html, null);
            
            // Load font from classpath using ClassPathResource (works in both runtime and tests)
            ClassPathResource fontResource = new ClassPathResource("fonts/PTMono-Regular.ttf");
            File tempFontFile = null;
            try (InputStream fontStream = fontResource.getInputStream()) {
                // Create temporary file for font (useFont requires a File)
                tempFontFile = File.createTempFile("PTMono-Regular", ".ttf");
                tempFontFile.deleteOnExit();
                Files.copy(fontStream, tempFontFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                builder.useFont(tempFontFile, "PT Mono");
            } finally {
                // Clean up temporary file
                if (tempFontFile != null && tempFontFile.exists()) {
                    tempFontFile.delete();
                }
            }
            
            builder.toStream(os);
            builder.run();

            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Error generating PDF from report", e);
        }
    }
}

