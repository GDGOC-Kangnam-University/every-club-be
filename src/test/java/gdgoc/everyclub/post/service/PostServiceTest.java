package gdgoc.everyclub.post.service;

import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.post.domain.Post;
import gdgoc.everyclub.post.dto.PostCreateRequest;
import gdgoc.everyclub.post.dto.PostUpdateRequest;
import gdgoc.everyclub.post.repository.PostRepository;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PostService postService;

    private User author;
    private Post post;

    @BeforeEach
    void setUp() {
        author = new User("Author", "author@example.com");
        ReflectionTestUtils.setField(author, "id", 1L);

        post = new Post("Title", "Content", author);
        ReflectionTestUtils.setField(post, "id", 1L);
    }

    @Test
    @DisplayName("게시글을 생성한다")
    void createPost() {
        // given
        PostCreateRequest request = new PostCreateRequest("Title", "Content", 1L);
        given(userService.getUserById(1L)).willReturn(author);
        given(postRepository.save(any(Post.class))).willAnswer(invocation -> {
            Post savedPost = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedPost, "id", 1L);
            return savedPost;
        });

        // when
        Long postId = postService.createPost(request);

        // then
        assertThat(postId).isEqualTo(1L);
        verify(postRepository).save(any(Post.class));
    }

    @Test
    @DisplayName("게시글을 전체 조회한다")
    void getPosts() {
        // given
        given(postRepository.findAllWithAuthor()).willReturn(List.of(post));

        // when
        List<Post> posts = postService.getPosts();

        // then
        assertThat(posts).hasSize(1);
        assertThat(posts.get(0).getTitle()).isEqualTo("Title");
    }

    @Test
    @DisplayName("ID로 게시글을 조회한다")
    void getPostById() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        Post foundPost = postService.getPostById(1L);

        // then
        assertThat(foundPost).isEqualTo(post);
    }

    @Test
    @DisplayName("존재하지 않는 게시글 조회 시 예외가 발생한다")
    void getPostById_NotFound() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> postService.getPostById(1L))
                .isInstanceOf(LogicException.class)
                .extracting("errorCode")
                .isEqualTo(ResourceErrorCode.RESOURCE_NOT_FOUND);
    }

    @Test
    @DisplayName("게시글을 수정한다")
    void updatePost() {
        // given
        PostUpdateRequest request = new PostUpdateRequest("New Title", "New Content");
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.updatePost(1L, request);

        // then
        assertThat(post.getTitle()).isEqualTo("New Title");
        assertThat(post.getContent()).isEqualTo("New Content");
    }

    @Test
    @DisplayName("게시글을 삭제한다")
    void deletePost() {
        // given
        given(postRepository.findById(1L)).willReturn(Optional.of(post));

        // when
        postService.deletePost(1L);

        // then
        verify(postRepository).delete(post);
    }
}
