package server.post.presentation

import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.post.application.CreatePostCommand
import server.post.application.CreatePostResult
import server.post.application.CreatePostImageUploadUrlCommand
import server.post.application.PostImageUploadUrlResult
import server.post.application.PostService

@RestController
@RequestMapping("/api/v1/post")
class PostController(
    private val postService: PostService,
) {
    @PostMapping
    fun create(
        @RequestBody command: CreatePostCommand,
    ): ApiResponse<CreatePostResult> = ApiResponse.of(postService.create(command))

    @PostMapping("/image/presigned-url")
    fun createImageUploadUrl(
        @RequestBody command: CreatePostImageUploadUrlCommand,
    ): ApiResponse<PostImageUploadUrlResult> = ApiResponse.of(postService.createImageUploadUrl(command))
}
