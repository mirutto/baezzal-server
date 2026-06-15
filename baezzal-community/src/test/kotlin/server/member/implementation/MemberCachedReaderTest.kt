package server.member.implementation

import global.error.NotFoundException
import global.image.ImageStatus
import global.image.ImageVersions
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.member.domain.Member
import server.member.domain.MemberProvider
import server.member.domain.MemberRole
import server.member.infrastructure.MemberCache

class MemberCachedReaderTest {
    private val memberReader = mockk<MemberReader>()
    private val memberCache = mockk<MemberCache>()
    private val memberCachedReader = MemberCachedReader(memberReader, memberCache)

    @Test
    fun `readById 는 캐시를 우선 조회한다`() {
        val cached = member(id = 1L, username = "cached-user")
        every { memberCache.getById(1L) } returns cached

        val result = memberCachedReader.readById(1L)

        result shouldBe cached
        verify(exactly = 1) { memberCache.getById(1L) }
        verify(exactly = 0) { memberReader.readById(any()) }
    }

    @Test
    fun `readById 는 캐시 miss 이면 db 조회 후 캐시에 저장한다`() {
        val member = member(id = 1L, username = "db-user")
        every { memberCache.getById(1L) } returns null
        every { memberReader.readById(1L) } returns member
        every { memberCache.set(member) } returns Unit

        val result = memberCachedReader.readById(1L)

        result shouldBe member
        verify(exactly = 1) { memberReader.readById(1L) }
        verify(exactly = 1) { memberCache.set(member) }
    }

    @Test
    fun `readByUsername 은 캐시를 우선 조회한다`() {
        val cached = member(id = 1L, username = "cached-user")
        every { memberCache.getByUsername("cached-user") } returns cached

        val result = memberCachedReader.readByUsername("cached-user")

        result shouldBe cached
        verify(exactly = 1) { memberCache.getByUsername("cached-user") }
        verify(exactly = 0) { memberReader.readByUsername(any()) }
    }

    @Test
    fun `readByUsername 은 member 가 없으면 예외를 던진다`() {
        every { memberCache.getByUsername("missing-user") } returns null
        every { memberReader.readByUsername("missing-user") } throws NotFoundException("회원을 찾을 수 없습니다")

        shouldThrow<NotFoundException> {
            memberCachedReader.readByUsername("missing-user")
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
        profileImage = ImageVersions(
            rawUrl = "https://example.com/profile.png",
            publicUrl = "https://example.com/profile.png",
            thumbnailUrl = "https://example.com/profile.png",
            status = ImageStatus.SUCCESS,
            aspectRatio = 1.0,
        ),
        description = "description",
        preferredTeamId = 3L,
        role = MemberRole.USER,
    )
}
