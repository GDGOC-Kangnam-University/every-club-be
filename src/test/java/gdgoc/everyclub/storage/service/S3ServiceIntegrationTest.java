package gdgoc.everyclub.storage.service;

import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.EnabledIf;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@EnabledIf(expression = "#{!'${s3.access-key}'.equals('dummy')}", loadContext = true)
class S3ServiceIntegrationTest {

    @Autowired
    private S3Service s3Service;

    @Test
    @DisplayName("실제 GCS 연동: 업로드용 Presigned URL 생성을 확인한다")
    void generateUploadUrlIntegrationTest() {
        // given
        PresignedUrlRequest request = PresignedUrlRequest.builder()
                .fileName("integration-test.txt")
                .contentType("text/plain")
                .build();

        // when
        PresignedUrlResponse response = s3Service.generateUploadUrl(request);

        // then
        assertThat(response.getPresignedUrl()).isNotBlank();
        assertThat(response.getPresignedUrl()).contains("storage.googleapis.com");
        assertThat(response.getPresignedUrl()).contains("everyclub");
        System.out.println("Generated Upload URL: " + response.getPresignedUrl());
    }

    @Test
    @DisplayName("실제 GCS 연동: 다운로드용 Presigned URL 생성을 확인한다")
    void generateDownloadUrlIntegrationTest() {
        // given
        String filePath = "test-folder/integration-test.txt";

        // when
        String url = s3Service.generateDownloadUrl(filePath);

        // then
        assertThat(url).isNotBlank();
        assertThat(url).contains("storage.googleapis.com");
        assertThat(url).contains("everyclub");
        System.out.println("Generated Download URL: " + url);
    }
}
