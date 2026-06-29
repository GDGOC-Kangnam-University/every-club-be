package gdgoc.everyclub.docs;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "springdoc.api-docs.enabled=true",
        "springdoc.swagger-ui.enabled=true"
})
class OpenApiEndpointTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /openapi.json — OpenAPI 스펙을 JSON으로 반환")
    void openapiJson() throws Exception {
        mockMvc.perform(get("/openapi.json"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openapi").value("3.1.0"))
                .andExpect(jsonPath("$.info.title").value("EveryClub API"))
                .andExpect(jsonPath("$.paths").isMap());
    }

    @Test
    @DisplayName("GET /swagger-ui/index.html — Swagger UI 페이지를 HTML로 반환")
    void swaggerUiIndex() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"));
    }

    @Test
    @DisplayName("GET /health — 서버 상태 확인 (회귀 방지)")
    void health() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    @DisplayName("GET /openapi.json — 인증 없이 접근 가능 (403 방지)")
    void openapiJsonNoAuth() throws Exception {
        mockMvc.perform(get("/openapi.json"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /swagger-ui/index.html — 인증 없이 접근 가능 (500 방지)")
    void swaggerUiIndexNoAuth() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
