package server.auth.implementation

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.auth.infrastructure.Oauth2StateCache
import java.util.UUID

class Oauth2StateManagerTest {
    private val oauth2StateCache = mockk<Oauth2StateCache>()
    private val oauth2StateManager = Oauth2StateManager(oauth2StateCache)

    @Test
    fun `redirect uri 를 저장하면서 uuid state 를 발급한다`() {
        val savedState = slot<String>()
        every {
            oauth2StateCache.save(capture(savedState), "https://app.baezzal.com/login/callback")
        } just runs

        val result = oauth2StateManager.issue("https://app.baezzal.com/login/callback")

        result shouldBe savedState.captured
        UUID.fromString(result).toString() shouldBe result
        verify(exactly = 1) {
            oauth2StateCache.save(result, "https://app.baezzal.com/login/callback")
        }
    }

    @Test
    fun `state 로 redirect uri 를 1회 조회한다`() {
        every { oauth2StateCache.take("state-token") } returns "https://app.baezzal.com/login/callback"

        val result = oauth2StateManager.take("state-token")

        result shouldBe "https://app.baezzal.com/login/callback"
        verify(exactly = 1) { oauth2StateCache.take("state-token") }
    }
}
