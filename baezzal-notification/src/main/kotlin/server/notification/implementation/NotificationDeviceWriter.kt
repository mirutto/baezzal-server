package server.notification.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.notification.domain.NotificationDevice
import server.notification.infrastructure.NotificationDeviceRepository

@Component
class NotificationDeviceWriter(
    private val notificationDeviceRepository: NotificationDeviceRepository,
) {
    @Transactional
    fun write(notificationDevice: NotificationDevice): NotificationDevice =
        notificationDeviceRepository.save(notificationDevice)
}
