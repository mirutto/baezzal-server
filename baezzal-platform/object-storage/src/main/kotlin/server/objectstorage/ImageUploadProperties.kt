package server.objectstorage

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "upload.image")
class ImageUploadProperties {
    var bucket: String = "baezzal-images"
    var prefix: String = "images"
    var presignedExpirySeconds: Int = 600
}
