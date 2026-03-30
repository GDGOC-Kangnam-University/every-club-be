package gdgoc.everyclub.storage.controller;

import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import gdgoc.everyclub.storage.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for handling file-related operations, such as requesting presigned URLs for upload and download.
 */
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Files", description = "파일 업로드/다운로드 API")
public class FileController {

    private final S3Service s3Service;

    /**
     * Endpoint to request a presigned URL for uploading a file.
     *
     * @param request DTO containing the file name and content type
     * @return ApiResponse containing the presigned URL
     */
    @PostMapping("/upload-url")
    @Operation(summary = "업로드 URL 발급", description = "파일 업로드를 위한 presigned URL을 발급합니다.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "업로드 URL 발급 요청 본문",
            content = @io.swagger.v3.oas.annotations.media.Content(
                    mediaType = "application/json",
                    examples = @io.swagger.v3.oas.annotations.media.ExampleObject(
                            name = "업로드 URL 요청 예시",
                            value = OpenApiExamples.PRESIGNED_UPLOAD_REQUEST
                    )
            )
    )
    public ApiResponse<PresignedUrlResponse> getUploadUrl(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody PresignedUrlRequest request) {
        return ApiResponse.success(s3Service.generateUploadUrl(request, userDetails.getUserId()));
    }

    /**
     * Endpoint to request a presigned URL for downloading a file.
     *
     * @param filePath The path of the file in storage
     * @return ApiResponse containing the presigned URL
     */
    @GetMapping("/download-url")
    @Operation(summary = "다운로드 URL 발급", description = "저장된 파일 다운로드를 위한 presigned URL을 발급합니다.")
    public ApiResponse<String> getDownloadUrl(
            @Parameter(hidden = true) @AuthenticationPrincipal CustomUserDetails userDetails,
            @Parameter(description = "저장된 파일 경로", example = "users/1/uuid-banner.png")
            @RequestParam String filePath) {
        return ApiResponse.success(s3Service.generateDownloadUrl(filePath, userDetails.getUserId()));
    }
}
