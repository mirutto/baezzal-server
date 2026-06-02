package server.notification.application

import server.notification.domain.NotificationDevice
import server.notification.domain.NotificationPlatform
import java.time.LocalDateTime

data class UpsertNotificationDeviceCommand(
    val platform: NotificationPlatform,
    val token: String,
)

data class NotificationDeviceData(
    val id: Long,
    val userId: Long?,
    val platform: NotificationPlatform,
    val token: String,
    val enabled: Boolean,
    val lastSeenAt: LocalDateTime,
    val createdAt: LocalDateTime?,
    val updatedAt: LocalDateTime?,
) {
    constructor(notificationDevice: NotificationDevice) : this(
        id = notificationDevice.id,
        userId = notificationDevice.userId,
        platform = notificationDevice.platform,
        token = notificationDevice.token,
        enabled = notificationDevice.enabled,
        lastSeenAt = notificationDevice.lastSeenAt,
        createdAt = notificationDevice.createdAt,
        updatedAt = notificationDevice.updatedAt,
    )
}
