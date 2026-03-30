package gdgoc.everyclub.storage.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.security.dto.CustomUserDetails;
import gdgoc.everyclub.security.jwt.JwtProvider;
import gdgoc.everyclub.storage.dto.PresignedUrlRequest;
import gdgoc.everyclub.storage.dto.PresignedUrlResponse;
import gdgoc.everyclub.storage.service.S3Service;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private S3Service s3Service;

    @MockitoBean
    private JwtProvider jwtProvider;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails createUserDetails() {
        return new CustomUserDetails(1L, "user@example.com", null, "ROLE_USER");
    }

    @Test
    @DisplayName("업로드 URL 요청 시 200 OK와 presigned URL을 반환한다")
    void getUploadUrl() throws Exception {
        // given
        PresignedUrlRequest request = new PresignedUrlRequest("test.jpg", "image/jpeg");
        PresignedUrlResponse response = new PresignedUrlResponse("https://mock-s3-url.com/put-url");
        given(s3Service.generateUploadUrl(any(PresignedUrlRequest.class), eq(1L))).willReturn(response);

        // when & then
        mockMvc.perform(post("/api/files/upload-url")
                        .with(user(createUserDetails()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.presignedUrl").value(response.getPresignedUrl()));
    }

    @Test
    @DisplayName("다운로드 URL 요청 시 200 OK와 presigned URL을 반환한다")
    void getDownloadUrl() throws Exception {
        // given
        String filePath = "users/1/uuid-test.jpg";
        String mockUrl = "https://mock-s3-url.com/get-url";
        given(s3Service.generateDownloadUrl(filePath, 1L)).willReturn(mockUrl);

        // when & then
        mockMvc.perform(get("/api/files/download-url")
                        .with(user(createUserDetails()))
                        .param("filePath", filePath))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(mockUrl));
    }
}