package global.uuid

import java.util.UUID

fun String.isUuid(): Boolean = try {
    UUID.fromString(this)
    true
} catch (_: IllegalArgumentException) {
    false
}
