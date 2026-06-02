package server.feed.presentation

import global.web.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.feed.application.FeedPostData
import server.feed.application.FeedPostDetailData
import server.feed.application.FeedService

@RestController
@RequestMapping("/feed")
class FeedController(
    private val feedService: FeedService,
) {
    @GetMapping
    fun findAll(): ApiResponse<List<FeedPostData>> = ApiResponse.of(
        feedService.findAll(),
    )

    @GetMapping("/posts/{postId}")
    fun findById(
        @PathVariable postId: Long,
    ): ApiResponse<FeedPostDetailData> = ApiResponse.of(
        feedService.findById(postId),
    )
}
