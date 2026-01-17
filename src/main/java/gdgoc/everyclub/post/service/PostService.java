package gdgoc.everyclub.post.service;

import gdgoc.everyclub.common.exception.LogicException;
import gdgoc.everyclub.common.exception.ResourceErrorCode;
import gdgoc.everyclub.post.domain.Post;
import gdgoc.everyclub.post.dto.PostCreateRequest;
import gdgoc.everyclub.post.dto.PostUpdateRequest;
import gdgoc.everyclub.post.repository.PostRepository;
import gdgoc.everyclub.user.domain.User;
import gdgoc.everyclub.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostService {
    private final PostRepository postRepository;
    private final UserService userService;

    @Transactional
    public Long createPost(PostCreateRequest request) {
        User author = userService.getUserById(request.authorId());
        Post post = new Post(request.title(), request.content(), author);
        postRepository.save(post);
        return post.getId();
    }

    public Page<Post> getPosts(Pageable pageable) {
        return postRepository.findAll(pageable);
    }

    public Post getPostById(Long id) {
        return postRepository.findByIdWithAuthor(id)
                .orElseThrow(() -> new LogicException(ResourceErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public void updatePost(Long id, PostUpdateRequest request) {
        Post post = getPostById(id);
        post.update(request.title(), request.content());
    }

    @Transactional
    public void deletePost(Long id) {
        Post post = getPostById(id);
        postRepository.delete(post);
    }
}
