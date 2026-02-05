package gdgoc.everyclub.club.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.club.domain.Club;
import gdgoc.everyclub.club.dto.ClubCreateRequest;
import gdgoc.everyclub.club.dto.ClubUpdateRequest;
import gdgoc.everyclub.club.service.ClubService;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = ClubController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class ClubControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ClubService clubService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("동아리 생성 요청 시 200 OK와 생성된 동아리 ID를 반환한다")
    void createClub() throws Exception {
        // given
        ClubCreateRequest request = new ClubCreateRequest("Title", "Content", 1L);
        given(clubService.createClub(any(ClubCreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @DisplayName("동아리 생성 요청 시 필수 값이 누락되면 400 Bad Request를 반환한다")
    void createClub_InvalidInput() throws Exception {
        // given: empty title is invalid because of @NotEmpty
        ClubCreateRequest request = new ClubCreateRequest("", "Content", 1L);

        // when & then
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("동아리 생성 요청 시 authorId가 null이면 400 Bad Request를 반환한다")
    void createClub_NullAuthorId() throws Exception {
        // given: null authorId is invalid because of @NotNull
        ClubCreateRequest request = new ClubCreateRequest("Title", "Content", null);

        // when & then
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("동아리 생성 요청 시 제목이 null이면 400 Bad Request를 반환한다")
    void createClub_NullTitle() throws Exception {
        // given
        ClubCreateRequest request = new ClubCreateRequest(null, "Content", 1L);

        // when & then
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("동아리 생성 요청 시 내용이 null이면 400 Bad Request를 반환한다")
    void createClub_NullContent() throws Exception {
        // given
        ClubCreateRequest request = new ClubCreateRequest("Title", null, 1L);

        // when & then
        mockMvc.perform(post("/clubs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("모든 동아리 조회 시 200 OK와 동아리 리스트를 반환한다")
    void getClubs() throws Exception {
        // given
        User author = new User("Author", "author@example.com");
        Club club = new Club("Title", "Content", author);
        ReflectionTestUtils.setField(club, "id", 1L);

        given(clubService.getClubs(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(club)));

        // when & then
        mockMvc.perform(get("/clubs")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content[0].title").value("Title"))
                .andExpect(jsonPath("$.data.content[0].authorName").value("Author"));
    }

    @Test
    @DisplayName("특정 동아리 조회 시 200 OK와 동아리 정보를 반환한다")
    void getClub() throws Exception {
        // given
        Long clubId = 1L;
        User author = new User("Author", "author@example.com");
        Club club = new Club("Title", "Content", author);
        ReflectionTestUtils.setField(club, "id", clubId);

        given(clubService.getClubById(clubId)).willReturn(club);

        // when & then
        mockMvc.perform(get("/clubs/{id}", clubId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("Title"))
                .andExpect(jsonPath("$.data.content").value("Content"));
    }

    @Test
    @DisplayName("존재하지 않는 동아리 조회 시 404 Not Found를 반환한다")
    void getClub_NotFound() throws Exception {
        // given
        Long clubId = 999L;
        given(clubService.getClubById(clubId))
                .willThrow(new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/clubs/{id}", clubId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("동아리 수정 시 200 OK를 반환한다")
    void updateClub() throws Exception {
        // given
        Long clubId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest("Updated Title", "Updated Content");

        // when & then
        mockMvc.perform(put("/clubs/{id}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubService).updateClub(eq(clubId), any(ClubUpdateRequest.class));
    }

    @Test
    @DisplayName("동아리 수정 시 필수 값이 누락되면 400 Bad Request를 반환한다")
    void updateClub_InvalidInput() throws Exception {
        // given
        Long clubId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest("", "");

        // when & then
        mockMvc.perform(put("/clubs/{id}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("동아리 수정 시 제목이 null이면 400 Bad Request를 반환한다")
    void updateClub_NullTitle() throws Exception {
        // given
        Long clubId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest(null, "Content");

        // when & then
        mockMvc.perform(put("/clubs/{id}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("동아리 수정 시 내용이 null이면 400 Bad Request를 반환한다")
    void updateClub_NullContent() throws Exception {
        // given
        Long clubId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest("Title", null);

        // when & then
        mockMvc.perform(put("/clubs/{id}", clubId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("동아리 삭제 시 200 OK를 반환한다")
    void deleteClub() throws Exception {
        // given
        Long clubId = 1L;

        // when & then
        mockMvc.perform(delete("/clubs/{id}", clubId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubService).deleteClub(clubId);
    }
}
