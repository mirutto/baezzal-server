package server.notification.presentation

import global.auth.Passport
import global.auth.RequestPassport
import global.web.ApiResponse
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import server.notification.application.NotificationDeviceData
import server.notification.application.NotificationDeviceService
import server.notification.application.UpsertNotificationDeviceCommand

@RestController
@RequestMapping("/api/v1/notification")
class NotificationDeviceController(
    private val notificationDeviceService: NotificationDeviceService,
) {
    @PostMapping("/devices")
    fun upsert(
        @RequestPassport passport: Passport,
        @RequestBody command: UpsertNotificationDeviceCommand,
    ): ApiResponse<NotificationDeviceData> = ApiResponse.of(
        notificationDeviceService.upsertDevice(
            userId = passport.memberId,
            command = command,
        ),
    )
}
