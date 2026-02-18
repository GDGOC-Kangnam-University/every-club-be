package gdgoc.everyclub.storage.service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import gdgoc.everyclub.storage.config.S3Properties;
import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.URL;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3ServiceTest {

    @Mock
    private AmazonS3 amazonS3;

    @Mock
    private S3Properties s3Properties;

    @InjectMocks
    private S3Service s3Service;

    @BeforeEach
    void setUp() {
        when(s3Properties.getBucket()).thenReturn("test-bucket");
        when(s3Properties.getExpirationMinutes()).thenReturn(5);
    }

    @Test
    void generateUploadUrlShouldReturnUrl() throws Exception {
        PresignedUrlRequest request = PresignedUrlRequest.builder()
                .fileName("test.jpg")
                .contentType("image/jpeg")
                .build();
        
        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/uuid-test.jpg?signed=true");
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(mockUrl);

        PresignedUrlResponse response = s3Service.generateUploadUrl(request);

        assertThat(response.getPresignedUrl()).isEqualTo(mockUrl.toString());
    }

    @Test
    void generateDownloadUrlShouldReturnUrl() throws Exception {
        String filePath = "test-folder/test.jpg";
        URL mockUrl = new URL("https://test-bucket.s3.amazonaws.com/test-folder/test.jpg?signed=true");
        when(amazonS3.generatePresignedUrl(any(GeneratePresignedUrlRequest.class)))
                .thenReturn(mockUrl);

        String result = s3Service.generateDownloadUrl(filePath);

        assertThat(result).isEqualTo(mockUrl.toString());
    }
}
