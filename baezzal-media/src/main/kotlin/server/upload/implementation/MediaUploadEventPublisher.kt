package server.upload.implementation

import org.springframework.stereotype.Component
import server.outbox.TransactionalEventPublisher
import server.upload.application.MediaUploadUrlIssuedEvent

@Component
class MediaUploadEventPublisher(
    private val transactionalEventPublisher: TransactionalEventPublisher,
) {
    fun publishIssued(event: MediaUploadUrlIssuedEvent) {
        transactionalEventPublisher.publish(event)
    }
}
