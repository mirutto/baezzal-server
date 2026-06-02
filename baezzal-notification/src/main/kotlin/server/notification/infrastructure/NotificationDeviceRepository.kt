package server.notification.infrastructure

import org.springframework.data.jpa.repository.JpaRepository
import server.notification.domain.NotificationDevice

interface NotificationDeviceRepository : JpaRepository<NotificationDevice, Long> {
    fun findByToken(token: String): NotificationDevice?
}
