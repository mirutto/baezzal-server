package server.member.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.member.application.CreateMemberProfileImageUploadUrlCommand
import server.member.application.MemberData
import server.member.application.MemberNicknameUpdateCommand
import server.member.application.MemberOnboardingCommand
import server.member.application.MemberProfileImageUpdateCommand
import server.member.application.MemberProfileImageUploadUrlResult
import server.member.application.MemberPreferredTeamUpdateCommand
import server.member.application.MemberService

@RestController
@RequestMapping("/api/v1/member")
class MemberController(
    private val memberService: MemberService,
) {
    @PostMapping("/onboarding")
    fun updateOnboarding(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberOnboardingCommand,
    ): ApiResponse<MemberData> = ApiResponse.of(
        memberService.onboarding(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @PostMapping("/nickname")
    fun updateNickname(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberNicknameUpdateCommand,
    ): ApiResponse<MemberData> = ApiResponse.of(
        memberService.updateNickname(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @PostMapping("/team")
    fun updatePreferredTeam(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberPreferredTeamUpdateCommand,
    ): ApiResponse<MemberData> = ApiResponse.of(
        memberService.updatePreferredTeam(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @PostMapping("/profile-image")
    fun updateProfileImage(
        @RequestPassport passport: Passport,
        @RequestBody command: MemberProfileImageUpdateCommand,
    ): ApiResponse<MemberData> = ApiResponse.of(
        memberService.updateProfileImage(
            memberId = passport.memberId,
            command = command,
        ),
    )

    @PostMapping("/profile-image/presigned-url")
    fun createProfileImageUploadUrl(
        @RequestPassport passport: Passport,
        @RequestBody command: CreateMemberProfileImageUploadUrlCommand,
    ): ApiResponse<MemberProfileImageUploadUrlResult> = ApiResponse.of(
        memberService.createProfileImageUploadUrl(
            memberId = passport.memberId,
            command = command,
        ),
    )
}
