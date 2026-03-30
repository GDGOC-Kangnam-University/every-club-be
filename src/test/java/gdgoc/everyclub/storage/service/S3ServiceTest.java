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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private S3Properties s3Properties;

    @InjectMocks
    private S3Service s3Service;

    private static final Long USER_ID = 1L;

    @BeforeEach
    void setUp() {
        lenient().when(s3Properties.getBucket()).thenReturn("test-bucket");
        lenient().when(s3Properties.getExpirationMinutes()).thenReturn(5);
    }

    @Test
    @DisplayName("업로드 URL 생성 - 사용자별 경로로 생성됨")
    void generateUploadUrlShouldReturnUrlWithUserPrefix() throws Exception {
        PresignedUrlRequest request = PresignedUrlRequest.builder()
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .build();

        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/users/1/uuid-test.jpg?signed=true");
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(mockUrl);

        PresignedUrlResponse response = s3Service.generateUploadUrl(request, USER_ID);

        assertThat(response.getPresignedUrl()).isEqualTo(mockUrl.toString());
    }

    @Test
    @DisplayName("다운로드 URL 생성 - 사용자 소유 경로만 접근 가능")
    void generateDownloadUrlShouldReturnUrlForUserOwnedPath() throws Exception {
        String filePath = "users/1/uuid-test.jpg";
        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/users/1/uuid-test.jpg?signed=true");
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(mockUrl);

        String result = s3Service.generateDownloadUrl(filePath, USER_ID);

        assertThat(result).isEqualTo(mockUrl.toString());
    }

    @Test
    @DisplayName("다운로드 URL - 다른 사용자 경로 접근 시 ACCESS_DENIED")
    void generateDownloadUrlShouldDenyOtherUserPath() {
        String otherUserPath = "users/2/uuid-test.jpg";

        assertThatThrownBy(() -> s3Service.generateDownloadUrl(otherUserPath, USER_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(AccessErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("다운로드 URL - path traversal 공격 차단")
    void generateDownloadUrlShouldBlockPathTraversal() {
        String traversalPath = "users/1/../../../other-folder/file.jpg";

        assertThatThrownBy(() -> s3Service.generateDownloadUrl(traversalPath, USER_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(AccessErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("다운로드 URL - 백슬래시 path traversal 차단")
    void generateDownloadUrlShouldBlockBackslashTraversal() {
        String traversalPath = "users\\1\\..\\..\\other";

        assertThatThrownBy(() -> s3Service.generateDownloadUrl(traversalPath, USER_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(AccessErrorCode.ACCESS_DENIED);
    }

    @Test
    @DisplayName("다운로드 URL - null 경로 시 INVALID_INPUT")
    void generateDownloadUrlShouldRejectNullPath() {
        assertThatThrownBy(() -> s3Service.generateDownloadUrl(null, USER_ID))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ValidationErrorCode.INVALID_INPUT);
    }

    @Test
    @DisplayName("업로드 URL - 파일명에서 path separator 제거")
    void generateUploadUrlShouldSanitizeFileName() throws Exception {
        PresignedUrlRequest request = PresignedUrlRequest.builder()
                .fileName("../../malicious.jpg")
                .contentType("image/jpeg")
                .build();

        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/users/1/uuid-__malicious.jpg?signed=true");
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(mockUrl);

        PresignedUrlResponse response = s3Service.generateUploadUrl(request, USER_ID);

        assertThat(response.getPresignedUrl()).isEqualTo(mockUrl.toString());
    }
}