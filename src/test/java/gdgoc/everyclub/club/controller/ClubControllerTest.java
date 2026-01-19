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
    @DisplayName("게시글 생성 요청 시 200 OK와 생성된 게시글 ID를 반환한다")
    void createPost() throws Exception {
        // given
        ClubCreateRequest request = new ClubCreateRequest("Title", "Content", 1L);
        given(clubService.createPost(any(ClubCreateRequest.class))).willReturn(1L);

        // when & then
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").value(1L));
    }

    @Test
    @DisplayName("게시글 생성 요청 시 필수 값이 누락되면 400 Bad Request를 반환한다")
    void createPost_InvalidInput() throws Exception {
        // given: empty title is invalid because of @NotEmpty
        ClubCreateRequest request = new ClubCreateRequest("", "Content", 1L);

        // when & then
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("게시글 생성 요청 시 authorId가 null이면 400 Bad Request를 반환한다")
    void createPost_NullAuthorId() throws Exception {
        // given: null authorId is invalid because of @NotNull
        ClubCreateRequest request = new ClubCreateRequest("Title", "Content", null);

        // when & then
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("게시글 생성 요청 시 제목이 null이면 400 Bad Request를 반환한다")
    void createPost_NullTitle() throws Exception {
        // given
        ClubCreateRequest request = new ClubCreateRequest(null, "Content", 1L);

        // when & then
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("게시글 생성 요청 시 내용이 null이면 400 Bad Request를 반환한다")
    void createPost_NullContent() throws Exception {
        // given
        ClubCreateRequest request = new ClubCreateRequest("Title", null, 1L);

        // when & then
        mockMvc.perform(post("/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("모든 게시글 조회 시 200 OK와 게시글 리스트를 반환한다")
    void getPosts() throws Exception {
        // given
        User author = new User("Author", "author@example.com");
        Club club = new Club("Title", "Content", author);
        ReflectionTestUtils.setField(club, "id", 1L);

        given(clubService.getPosts(any(PageRequest.class))).willReturn(new PageImpl<>(List.of(club)));

        // when & then
        mockMvc.perform(get("/posts")
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
    @DisplayName("특정 게시글 조회 시 200 OK와 게시글 정보를 반환한다")
    void getPost() throws Exception {
        // given
        Long postId = 1L;
        User author = new User("Author", "author@example.com");
        Club club = new Club("Title", "Content", author);
        ReflectionTestUtils.setField(club, "id", postId);

        given(clubService.getPostById(postId)).willReturn(club);

        // when & then
        mockMvc.perform(get("/posts/{id}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.title").value("Title"))
                .andExpect(jsonPath("$.data.content").value("Content"));
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 404 Not Found를 반환한다")
    void getPost_NotFound() throws Exception {
        // given
        Long postId = 999L;
        given(clubService.getPostById(postId))
                .willThrow(new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/posts/{id}", postId))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("게시글 수정 시 200 OK를 반환한다")
    void updatePost() throws Exception {
        // given
        Long postId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest("Updated Title", "Updated Content");

        // when & then
        mockMvc.perform(put("/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubService).updatePost(eq(postId), any(ClubUpdateRequest.class));
    }

    @Test
    @DisplayName("게시글 수정 시 필수 값이 누락되면 400 Bad Request를 반환한다")
    void updatePost_InvalidInput() throws Exception {
        // given
        Long postId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest("", "");

        // when & then
        mockMvc.perform(put("/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("게시글 수정 시 제목이 null이면 400 Bad Request를 반환한다")
    void updatePost_NullTitle() throws Exception {
        // given
        Long postId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest(null, "Content");

        // when & then
        mockMvc.perform(put("/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("게시글 수정 시 내용이 null이면 400 Bad Request를 반환한다")
    void updatePost_NullContent() throws Exception {
        // given
        Long postId = 1L;
        ClubUpdateRequest request = new ClubUpdateRequest("Title", null);

        // when & then
        mockMvc.perform(put("/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"));
    }

    @Test
    @DisplayName("게시글 삭제 시 200 OK를 반환한다")
    void deletePost() throws Exception {
        // given
        Long postId = 1L;

        // when & then
        mockMvc.perform(delete("/posts/{id}", postId))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(clubService).deletePost(postId);
    }
}
