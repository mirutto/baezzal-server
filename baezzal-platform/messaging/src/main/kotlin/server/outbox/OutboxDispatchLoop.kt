package server.outbox

import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.springframework.stereotype.Component
import kotlin.time.Duration.Companion.milliseconds

@Component
class OutboxDispatchLoop(
    private val outboxEventDispatcher: OutboxEventDispatcher,
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var loopJob: Job? = null

    @PostConstruct
    fun start() {
        if (loopJob?.isActive == true) return
        loopJob =
            scope.launch {
                while (isActive) {
                    outboxEventDispatcher.dispatchBatch(batchSize = 200)
                    delay(1_000.milliseconds)
                }
            }
    }

    @PreDestroy
    fun stop() {
        loopJob?.cancel()
        loopJob = null
        scope.cancel()
    }
}
