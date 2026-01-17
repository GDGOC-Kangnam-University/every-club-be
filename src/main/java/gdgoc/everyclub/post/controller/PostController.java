package gdgoc.everyclub.post.controller;

import gdgoc.everyclub.common.ApiResponse;
import gdgoc.everyclub.post.dto.PostCreateRequest;
import gdgoc.everyclub.post.dto.PostResponse;
import gdgoc.everyclub.post.dto.PostUpdateRequest;
import gdgoc.everyclub.post.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public ApiResponse<Long> createPost(@RequestBody @Valid PostCreateRequest request) {
        Long id = postService.createPost(request);
        return ApiResponse.success(id);
    }

    @GetMapping
    public ApiResponse<Page<PostResponse>> getPosts(Pageable pageable) {
        Page<PostResponse> responses = postService.getPosts(pageable)
                .map(PostResponse::new);
        return ApiResponse.success(responses);
    }

    @GetMapping("/{id}")
    public ApiResponse<PostResponse> getPost(@PathVariable Long id) {
        PostResponse response = new PostResponse(postService.getPostById(id));
        return ApiResponse.success(response);
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updatePost(@PathVariable Long id, @RequestBody @Valid PostUpdateRequest request) {
        postService.updatePost(id, request);
        return ApiResponse.success();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ApiResponse.success();
    }
}
