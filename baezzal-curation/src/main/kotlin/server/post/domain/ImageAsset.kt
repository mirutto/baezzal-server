package server.post.domain

import jakarta.persistence.Column
import jakarta.persistence.Embeddable

@Embeddable
class ImageAsset(
    @Column(length = 2048)
    val url: String = "",

    @Column
    val width: Int? = null,

    @Column
    val height: Int? = null,

    @Column(name = "aspect_ratio")
    val aspectRatio: Double? = null,
)
