package szte.flowboard.service.report;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

@Service
public class PDFGenerator {

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

