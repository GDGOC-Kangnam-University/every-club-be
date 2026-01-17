package gdgoc.everyclub.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.post.domain.Post;
import gdgoc.everyclub.post.dto.PostCreateRequest;
import gdgoc.everyclub.post.dto.PostUpdateRequest;
import gdgoc.everyclub.post.service.PostService;
import gdgoc.everyclub.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = PostController.class, excludeAutoConfiguration = SecurityAutoConfiguration.class)
class PostControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("게시글 생성 요청 시 200 OK와 생성된 게시글 ID를 반환한다")
    void createPost() throws Exception {
        // given
        PostCreateRequest request = new PostCreateRequest("Title", "Content", 1L);
        given(postService.createPost(any(PostCreateRequest.class))).willReturn(1L);

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
    @DisplayName("모든 게시글 조회 시 200 OK와 게시글 리스트를 반환한다")
    void getPosts() throws Exception {
        // given
        User author = new User("Author", "author@example.com");
        Post post = new Post("Title", "Content", author);
        ReflectionTestUtils.setField(post, "id", 1L);
        
        given(postService.getPosts()).willReturn(List.of(post));

        // when & then
        mockMvc.perform(get("/posts"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("Title"))
                .andExpect(jsonPath("$.data[0].authorName").value("Author"));
    }

    @Test
    @DisplayName("특정 게시글 조회 시 200 OK와 게시글 정보를 반환한다")
    void getPost() throws Exception {
        // given
        Long postId = 1L;
        User author = new User("Author", "author@example.com");
        Post post = new Post("Title", "Content", author);
        ReflectionTestUtils.setField(post, "id", postId);
        
        given(postService.getPostById(postId)).willReturn(post);

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
        given(postService.getPostById(postId))
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
        PostUpdateRequest request = new PostUpdateRequest("Updated Title", "Updated Content");

        // when & then
        mockMvc.perform(put("/posts/{id}", postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"));

        verify(postService).updatePost(eq(postId), any(PostUpdateRequest.class));
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

        verify(postService).deletePost(postId);
    }
}
