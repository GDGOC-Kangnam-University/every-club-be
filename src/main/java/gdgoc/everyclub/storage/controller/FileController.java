package gdgoc.everyclub.storage.controller;

import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import gdgoc.everyclub.storage.service.S3Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileController implements FileApiSpec {

    private final S3Service s3Service;

    @Override
    public ApiResponse<PresignedUrlResponse> getUploadUrl(@Valid @RequestBody PresignedUrlRequest request) {
        return ApiResponse.success(s3Service.generateUploadUrl(request));
    }

    @Override
    public ApiResponse<String> getDownloadUrl(String filePath) {
        return ApiResponse.success(s3Service.generateDownloadUrl(filePath));
    }
}
