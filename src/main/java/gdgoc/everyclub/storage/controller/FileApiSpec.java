package gdgoc.everyclub.storage.controller;

import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.docs.FileDocs;
import gdgoc.everyclub.docs.OpenApiExamples;
import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = FileDocs.TAG_NAME, description = FileDocs.TAG_DESCRIPTION)
public interface FileApiSpec {

    @PostMapping("/upload-url")
    @Operation(summary = "업로드 URL 발급", description = "파일 업로드를 위한 presigned URL을 발급합니다.")
    @RequestBody(
            required = true,
            description = "업로드 URL 발급 요청 본문",
            content = @Content(
                    mediaType = "application/json",
                    examples = @ExampleObject(
                            name = "업로드 URL 요청 예시",
                            value = OpenApiExamples.PRESIGNED_UPLOAD_REQUEST
                    )
            )
    )
    ApiResponse<PresignedUrlResponse> getUploadUrl(PresignedUrlRequest request);

    @GetMapping("/download-url")
    @Operation(summary = "다운로드 URL 발급", description = "저장된 파일 다운로드를 위한 presigned URL을 발급합니다.")
    ApiResponse<String> getDownloadUrl(
            @Parameter(description = "저장된 파일 경로", example = "clubs/gdgoc/banner.png")
            @RequestParam String filePath);
}
