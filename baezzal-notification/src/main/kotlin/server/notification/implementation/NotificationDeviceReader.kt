package server.notification.implementation

import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import server.notification.domain.NotificationDevice
import server.notification.infrastructure.NotificationDeviceRepository

@Component
class NotificationDeviceReader(
    private val notificationDeviceRepository: NotificationDeviceRepository,
) {
    @Transactional(readOnly = true)
    fun readByToken(token: String): NotificationDevice? =
        notificationDeviceRepository.findByToken(token)
}
