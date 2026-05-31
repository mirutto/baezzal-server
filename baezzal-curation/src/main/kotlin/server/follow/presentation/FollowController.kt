package server.follow.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.follow.application.FollowResult
import server.follow.application.FollowService

@RestController
@RequestMapping("/api/v1/follow")
class FollowController(
    private val followService: FollowService,
) {
    @PostMapping("/{followeeId}")
    fun follow(
        @RequestPassport passport: Passport,
        @PathVariable followeeId: Long,
    ): ApiResponse<FollowResult> = ApiResponse.of(
        followService.follow(
            followerId = passport.memberId,
            followeeId = followeeId,
        ),
    )

    @DeleteMapping("/{followeeId}")
    fun unfollow(
        @RequestPassport passport: Passport,
        @PathVariable followeeId: Long,
    ): ApiResponse<FollowResult> = ApiResponse.of(
        followService.unfollow(
            followerId = passport.memberId,
            followeeId = followeeId,
        ),
    )
}
