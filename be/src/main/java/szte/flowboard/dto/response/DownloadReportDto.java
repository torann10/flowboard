package szte.flowboard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;

/**
 * Response DTO for report download URL.
 * Contains a presigned URL for downloading a report from S3.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DownloadReportDto {

    /** The presigned URL for downloading the report from S3 (valid for 5 minutes) */
    private URL downloadUrl;
}



