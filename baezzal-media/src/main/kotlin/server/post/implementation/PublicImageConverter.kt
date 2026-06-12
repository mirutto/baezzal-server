package server.post.implementation

import org.springframework.stereotype.Component
import server.image.application.ImageData
import server.image.implementation.ImageConverter

@Component
class PublicImageConverter(
    private val imageConverter: ImageConverter,
) {

    fun convert(imageData: ImageData): ImageData =
        imageConverter.convert(imageData)
}
