package szte.flowboard.service;

import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.net.URL;
import java.time.Duration;
import java.util.UUID;

/**
 * Service for interacting with AWS S3 for report storage.
 * Handles uploading, deleting, and generating presigned download URLs for report PDFs.
 * Uses the flowboard-report-bucket for storing reports.
 */
@Service
public class S3Service {

    private static final String BUCKET_NAME = "flowboard-report-bucket";

    /**
     * Uploads a report PDF to S3.
     *
     * @param reportId the unique identifier of the report (used as S3 key)
     * @param data the PDF data to upload
     * @return true if upload succeeds, false otherwise
     */
    public boolean uploadReport(UUID reportId, byte[] data) {
        try (var s3Client = getS3ClientBuilder().build()) {
            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(reportId.toString())
                    .build();

            s3Client.putObject(objectRequest, RequestBody.fromBytes(data));
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    /**
     * Deletes a report PDF from S3.
     *
     * @param reportId the unique identifier of the report (used as S3 key)
     * @return true if deletion succeeds, false otherwise
     */
    public boolean deleteReport(UUID reportId) {
        try (var s3Client = getS3ClientBuilder().build()) {
            var deleteObject = DeleteObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(reportId.toString())
                    .build();

            s3Client.deleteObject(deleteObject);
            return true;
        } catch (S3Exception e) {
            return false;
        }
    }

    /**
     * Generates a presigned download URL for a report from S3.
     * The URL is valid for 5 minutes.
     *
     * @param reportId the unique identifier of the report (used as S3 key)
     * @param contentDisposition the Content-Disposition header value for the download
     * @param contentType the Content-Type header value (typically "application/pdf")
     * @return a presigned URL for downloading the report, or null if generation fails
     */
    public URL getDownloadUrl(UUID reportId, String contentDisposition, String contentType) {
        try (var s3Presigner = getS3Presigner()) {
            var objectRequest = GetObjectRequest.builder()
                    .bucket(BUCKET_NAME)
                    .key(reportId.toString())
                    .responseContentDisposition(contentDisposition)
                    .responseContentType(contentType)
                    .build();

            var presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(5))
                    .getObjectRequest(objectRequest)
                    .build();

            var result = s3Presigner.presignGetObject(presignRequest);
            return result.url();
        } catch (S3Exception e) {
            return null;
        }
    }

    /**
     * Creates an S3 client builder configured with default credentials and EU Central 1 region.
     *
     * @return an S3ClientBuilder instance
     */
    private S3ClientBuilder getS3ClientBuilder() {
        return S3Client.builder()
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .region(Region.EU_CENTRAL_1);
    }

    /**
     * Creates an S3 presigner configured with default credentials and EU Central 1 region.
     *
     * @return an S3Presigner instance
     */
    private S3Presigner getS3Presigner() {
        return S3Presigner.builder()
                .credentialsProvider(DefaultCredentialsProvider.builder().build())
                .region(Region.EU_CENTRAL_1)
                .build();
    }
}

