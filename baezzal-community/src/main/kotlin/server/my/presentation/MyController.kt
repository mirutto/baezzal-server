package server.my.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.member.application.MemberData
import server.my.application.MyFollowService
import server.my.application.MyFollowStats
import server.my.application.MyMemberResult
import server.my.application.MyMemberService

@RestController
@RequestMapping("/api/v1/my")
class MyController(
    private val myMemberService: MyMemberService,
    private val myFollowService: MyFollowService,
) {
    @GetMapping("/profile")
    fun getMyProfile(
        @RequestPassport passport: Passport,
    ): ApiResponse<MyMemberResult> = ApiResponse.of(
        myMemberService.getMyProfile(passport.memberId),
    )

    @GetMapping("/follow/stats")
    fun getMyFollowStats(
        @RequestPassport passport: Passport,
    ): ApiResponse<MyFollowStats> = ApiResponse.of(
        myFollowService.getMyStats(passport.memberId),
    )

    @GetMapping("/followers")
    fun getMyFollowers(
        @RequestPassport passport: Passport,
    ): ApiResponse<List<MemberData>> = ApiResponse.of(
        myFollowService.getMyFollowers(passport.memberId),
    )

    @GetMapping("/followings")
    fun getMyFollowings(
        @RequestPassport passport: Passport,
    ): ApiResponse<List<MemberData>> = ApiResponse.of(
        myFollowService.getMyFollowings(passport.memberId),
    )
}
