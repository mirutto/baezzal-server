package server.notification.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.notification.domain.NotificationDevice
import server.notification.domain.NotificationPlatform
import server.notification.implementation.NotificationDeviceReader
import server.notification.implementation.NotificationDeviceWriter
import java.time.LocalDateTime

class NotificationDeviceServiceTest {
    private val notificationDeviceReader = mockk<NotificationDeviceReader>()
    private val notificationDeviceWriter = mockk<NotificationDeviceWriter>()
    private val notificationDeviceService = NotificationDeviceService(
        notificationDeviceReader = notificationDeviceReader,
        notificationDeviceWriter = notificationDeviceWriter,
    )

    @Test
    fun `새 토큰이면 기기를 생성한다`() {
        val notificationDevice = slot<NotificationDevice>()
        every { notificationDeviceReader.readByToken("fcm-token") } returns null
        every { notificationDeviceWriter.write(capture(notificationDevice)) } answers { firstArg() }

        val result = notificationDeviceService.upsertDevice(
            userId = 1L,
            command = UpsertNotificationDeviceCommand(
                platform = NotificationPlatform.IOS,
                token = "fcm-token",
            ),
        )

        result.userId shouldBe 1L
        result.platform shouldBe NotificationPlatform.IOS
        result.token shouldBe "fcm-token"
        result.enabled shouldBe true
        notificationDevice.captured.userId shouldBe 1L
        notificationDevice.captured.platform shouldBe NotificationPlatform.IOS
        notificationDevice.captured.token shouldBe "fcm-token"
        notificationDevice.captured.enabled shouldBe true
    }

    @Test
    fun `기존 토큰이면 기기 정보를 갱신한다`() {
        val existingDevice = NotificationDevice(
            id = 3L,
            userId = null,
            platform = NotificationPlatform.WEB,
            token = "fcm-token",
            enabled = false,
            lastSeenAt = LocalDateTime.of(2026, 1, 1, 0, 0),
        )
        every { notificationDeviceReader.readByToken("fcm-token") } returns existingDevice
        every { notificationDeviceWriter.write(existingDevice) } returns existingDevice

        val result = notificationDeviceService.upsertDevice(
            userId = 2L,
            command = UpsertNotificationDeviceCommand(
                platform = NotificationPlatform.ANDROID,
                token = "fcm-token",
            ),
        )

        result.id shouldBe 3L
        result.userId shouldBe 2L
        result.platform shouldBe NotificationPlatform.ANDROID
        result.token shouldBe "fcm-token"
        result.enabled shouldBe true
        existingDevice.userId shouldBe 2L
        existingDevice.platform shouldBe NotificationPlatform.ANDROID
        existingDevice.enabled shouldBe true
    }
}
