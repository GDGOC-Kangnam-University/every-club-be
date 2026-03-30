package gdgoc.everyclub.storage.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import gdgoc.everyclub.common.exception.AccessErrorCode;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ValidationErrorCode;
import gdgoc.everyclub.storage.config.S3Properties;
import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Service for handling S3-compatible storage operations, specifically for generating presigned URLs.
 */
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;
    private final S3Properties s3Properties;

    /**
     * Generates a presigned URL for uploading a file using the PUT method.
     *
     * @param request DTO containing file name and content type
     * @param userId the ID of the user requesting the upload
     * @return DTO containing the generated presigned URL
     */
    public PresignedUrlResponse generateUploadUrl(PresignedUrlRequest request, Long userId) {
        String fileName = sanitizeFileName(request.getFileName());
        String filePath = createPathForUser(fileName, userId);
        Date expiration = getExpirationDate();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(s3Properties.getBucket(), filePath)
                        .withMethod(HttpMethod.PUT)
                        .withExpiration(expiration)
                        .withContentType(request.getContentType());

        String url = amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();

        return PresignedUrlResponse.builder()
                .presignedUrl(url)
                .build();
    }

    /**
     * Generates a presigned URL for downloading a file using the GET method.
     *
     * @param filePath The path to the file in the bucket
     * @param userId the ID of the user requesting the download
     * @return The generated presigned URL as a string
     */
    public String generateDownloadUrl(String filePath, Long userId) {
        validatePathOwnership(filePath, userId);
        Date expiration = getExpirationDate();

        GeneratePresignedUrlRequest generatePresignedUrlRequest =
                new GeneratePresignedUrlRequest(s3Properties.getBucket(), filePath)
                        .withMethod(HttpMethod.GET)
                        .withExpiration(expiration);

        return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
    }

    private Date getExpirationDate() {
        Date expiration = new Date();
        long expTimeMillis = expiration.getTime();
        expTimeMillis += 1000L * 60 * s3Properties.getExpirationMinutes();
        expiration.setTime(expTimeMillis);
        return expiration;
    }

    private String createPathForUser(String fileName, Long userId) {
        String sanitized = sanitizeFileName(fileName);
        return String.format("users/%d/%s", userId, UUID.randomUUID() + "-" + sanitized);
    }

    private void validatePathOwnership(String filePath, Long userId) {
        if (filePath == null || filePath.isBlank()) {
            throw new LogicException(ValidationErrorCode.INVALID_INPUT);
        }
        if (filePath.contains("..") || filePath.contains("\\")) {
            throw new LogicException(AccessErrorCode.ACCESS_DENIED);
        }
        String expectedPrefix = "users/" + userId + "/";
        if (!filePath.startsWith(expectedPrefix)) {
            throw new LogicException(AccessErrorCode.ACCESS_DENIED);
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            throw new LogicException(ValidationErrorCode.INVALID_INPUT);
        }
        return fileName.replace("/", "_").replace("\\", "_").replace("..", "_");
    }
}
