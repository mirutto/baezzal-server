package server.member.implementation

import global.error.BadRequestException
import io.kotest.assertions.throwables.shouldThrow
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import server.member.infrastructure.MemberProfileImageUrlCache

class MemberProfileImageValidatorTest {
    private val memberProfileImageUrlCache = mockk<MemberProfileImageUrlCache>()
    private val memberProfileImageValidator = MemberProfileImageValidator(memberProfileImageUrlCache)

    @Test
    fun `profile image presigned url 로 생성되지 않은 image url 이면 예외가 발생한다`() {
        every { memberProfileImageUrlCache.isIssued("https://cdn.example.com/profile.png") } returns false

        shouldThrow<BadRequestException> {
            memberProfileImageValidator.validateImageUrl("https://cdn.example.com/profile.png")
        }
    }

    @Test
    fun `profile image presigned url 로 생성된 image url 이면 통과한다`() {
        every { memberProfileImageUrlCache.isIssued("https://cdn.example.com/profile.png") } returns true

        memberProfileImageValidator.validateImageUrl("https://cdn.example.com/profile.png")
    }
}
