package server.post.presentation

import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.post.application.PostBatchResult
import server.post.application.PostBatchService

@RestController
@RequestMapping("/post")
class PostBatchController(
    private val postBatchService: PostBatchService,
) {
    @PostMapping("/batch")
    fun batch(): ApiResponse<PostBatchResult> = ApiResponse.of(
        postBatchService.updateViewCounts(),
    )
}
