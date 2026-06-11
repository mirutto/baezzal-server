package server.member.implementation

import global.error.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.infrastructure.MemberRepository
import java.util.Optional

class MemberReaderTest {
    private val memberRepository = mockk<MemberRepository>()
    private val memberReader = MemberReader(memberRepository)

    @Test
    fun `readById 는 db 에서 member 를 조회한다`() {
        val member = member(id = 1L, username = "db-user")
        every { memberRepository.findById(1L) } returns Optional.of(member)

        val result = memberReader.readById(1L)

        result shouldBe member
        verify(exactly = 1) { memberRepository.findById(1L) }
    }

    @Test
    fun `readByUsername 은 db 에서 member 를 조회한다`() {
        val member = member(id = 1L, username = "db-user")
        every { memberRepository.findByUsername("db-user") } returns member

        val result = memberReader.readByUsername("db-user")

        result shouldBe member
        verify(exactly = 1) { memberRepository.findByUsername("db-user") }
    }

    @Test
    fun `readByUsername 은 member 가 없으면 예외를 던진다`() {
        every { memberRepository.findByUsername("missing-user") } returns null

        shouldThrow<NotFoundException> {
            memberReader.readByUsername("missing-user")
        }
    }

    private fun member(
        id: Long,
        username: String,
    ): Member = Member(
        id = id,
        nickname = "nickname",
        username = username,
        provider = MemberProvider.GOOGLE,
        providerKey = "provider-key",
        profileImage = "https://example.com/profile.png",
        description = "description",
        preferredTeamId = 3L,
        role = MemberRole.USER,
    )
}
