package server

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["server", "global"])
class Application

fun main(args: Array<String>) {
    runApplication<Application>(*args)
}
