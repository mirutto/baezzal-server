package server.member.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.member.application.MemberData
import server.member.application.MemberIdResult
import server.member.application.MemberNicknameUpdateCommand
import server.member.application.MemberOnboardingCommand
import server.member.application.MemberPreferredTeamUpdateCommand
import server.member.application.MemberProfileImageUpdateCommand
import server.member.application.MemberService

@RestController
@RequestMapping("/api/v1/member")
class MemberController(
    private val memberService: MemberService,
) {
    @GetMapping("/{username}")
    fun getMember(
        @PathVariable username: String,
    ): ApiResponse<MemberData> = ApiResponse.of(
        memberService.findByUsername(username),
    )

    @PostMapping("/onboarding")
    fun updateOnboarding(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberOnboardingCommand,
    ): ApiResponse<MemberIdResult> = ApiResponse.of(
        memberService.onboarding(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @PostMapping("/nickname")
    fun updateNickname(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberNicknameUpdateCommand,
    ): ApiResponse<MemberIdResult> = ApiResponse.of(
        memberService.updateNickname(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @PostMapping("/team")
    fun updatePreferredTeam(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberPreferredTeamUpdateCommand,
    ): ApiResponse<MemberIdResult> = ApiResponse.of(
        memberService.updatePreferredTeam(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @PostMapping("/profile-image")
    fun updateProfileImage(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberProfileImageUpdateCommand,
    ): ApiResponse<MemberIdResult> = ApiResponse.of(
        memberService.updateProfileImage(
            memberId = passport.memberId,
            command = command,
        ),
    )
}
