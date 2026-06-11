package server.follow.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.follow.application.FollowResult
import server.follow.application.FollowService
import server.follow.application.MemberFollowSummaryResult
import server.follow.application.MyFollowStats

@RestController
@RequestMapping("/api/v1/follow")
class FollowController(
    private val followService: FollowService,
) {
    @GetMapping("/me/stats")
    fun myStats(
        @RequestPassport passport: Passport,
    ): ApiResponse<MyFollowStats> = ApiResponse.of(
        followService.myStats(passport.memberId),
    )

    @GetMapping("/{username}")
    fun getMemberFollowSummary(
        @RequestPassport passport: Passport,
        @PathVariable username: String,
    ): ApiResponse<MemberFollowSummaryResult> = ApiResponse.of(
        followService.getMemberFollowSummary(
            memberId = passport.memberId,
            targetUsername = username,
        ),
    )

    @PostMapping("/{username}")
    fun follow(
        @RequestPassport passport: Passport,
        @PathVariable username: String,
    ): ApiResponse<FollowResult> = ApiResponse.of(
        followService.follow(
            followerId = passport.memberId,
            followeeUsername = username,
        ),
    )

    @DeleteMapping("/{username}")
    fun unfollow(
        @RequestPassport passport: Passport,
        @PathVariable username: String,
    ): ApiResponse<FollowResult> = ApiResponse.of(
        followService.unfollow(
            followerId = passport.memberId,
            followeeUsername = username,
        ),
    )
}
