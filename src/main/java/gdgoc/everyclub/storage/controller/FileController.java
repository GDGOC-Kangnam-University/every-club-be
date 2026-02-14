package gdgoc.everyclub.storage.controller;

import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import gdgoc.everyclub.storage.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling file-related operations, such as requesting presigned URLs for upload and download.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController {

    private final S3Service s3Service;

    /**
     * Endpoint to request a presigned URL for uploading a file.
     *
     * @param request DTO containing the file name and content type
     * @return ApiResponse containing the presigned URL
     */
    @PostMapping("/upload-url")
    public ApiResponse<PresignedUrlResponse> getUploadUrl(@Valid @RequestBody PresignedUrlRequest request) {
        return ApiResponse.success(s3Service.generateUploadUrl(request));
    }

    /**
     * Endpoint to request a presigned URL for downloading a file.
     *
     * @param filePath The path of the file in storage
     * @return ApiResponse containing the presigned URL
     */
    @GetMapping("/download-url")
    public ApiResponse<String> getDownloadUrl(@RequestParam String filePath) {
        return ApiResponse.success(s3Service.generateDownloadUrl(filePath));
    }
}
