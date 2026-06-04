package server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication(scanBasePackages = ["server", "global"])
@EnableScheduling
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
