package server.post.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.post.application.CreatePostCommand
import server.post.application.PostService
import server.post.application.PostIdResult

@RestController
@RequestMapping("/api/v1/post")
class PostController(
    private val postService: PostService,
) {
    @PostMapping
    fun create(
        @RequestPassport passport: Passport,
        @RequestBody command: CreatePostCommand,
    ): ApiResponse<PostIdResult> = ApiResponse.of(
        postService.create(
            memberId = passport.memberId,
            command = command,
        ),
    )
}
