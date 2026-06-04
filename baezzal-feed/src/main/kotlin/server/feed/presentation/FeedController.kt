package server.feed.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.feed.application.FeedPostData
import server.feed.application.FeedPostDetailData
import server.feed.application.FeedService
import server.feed.application.FeedTeamSummaryData

@RestController
@RequestMapping("/feed")
class FeedController(
    private val feedService: FeedService,
) {
    @GetMapping
    fun findAll(): ApiResponse<List<FeedPostData>> = ApiResponse.of(
        feedService.findAll(),
    )

    @GetMapping("/teams")
    fun findTeams(): ApiResponse<List<FeedTeamSummaryData>> = ApiResponse.of(
        feedService.findTeams(),
    )

    @GetMapping("/posts/{postId}")
    fun findById(
        @RequestPassport passport: Passport?,
        @PathVariable postId: Long,
    ): ApiResponse<FeedPostDetailData> = ApiResponse.of(
        feedService.findById(
            postId = postId,
            memberId = passport?.memberId,
        ),
    )
}
