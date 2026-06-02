package server.notification.application

import global.error.BadRequestException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.notification.domain.NotificationDevice
import server.notification.implementation.NotificationDeviceReader
import server.notification.implementation.NotificationDeviceWriter
import java.time.LocalDateTime

@Service
class NotificationDeviceService(
    private val notificationDeviceReader: NotificationDeviceReader,
    private val notificationDeviceWriter: NotificationDeviceWriter,
) {
    @Transactional
    fun upsertDevice(
        userId: Long,
        command: UpsertNotificationDeviceCommand,
    ): NotificationDeviceData {
        val token = command.token.trim()
            .takeIf { it.isNotBlank() }
            ?: throw BadRequestException("토큰은 비어 있을 수 없습니다")
        val now = LocalDateTime.now()
        val notificationDevice = notificationDeviceReader.readByToken(token)
            ?.apply {
                refresh(
                    userId = userId,
                    platform = command.platform,
                    lastSeenAt = now,
                )
            }
            ?: NotificationDevice(
                userId = userId,
                platform = command.platform,
                token = token,
                enabled = true,
                lastSeenAt = now,
            )

        val savedNotificationDevice = notificationDeviceWriter.write(notificationDevice)

        // 푸시 알림 플랫폼(FCM/APNs/Web Push) 도입 후 이 지점에서 기기 등록/갱신 이벤트를 발행한다.

        return NotificationDeviceData(savedNotificationDevice)
    }
}
