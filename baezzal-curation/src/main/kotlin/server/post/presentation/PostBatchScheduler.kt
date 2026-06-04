package server.post.presentation

import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import server.post.application.PostBatchService

@Component
class PostBatchScheduler(
    private val postBatchService: PostBatchService,
) {
    @Scheduled(fixedDelay = 60_000)
    fun batchViewCounts() {
        postBatchService.updateViewCounts()
    }
}
