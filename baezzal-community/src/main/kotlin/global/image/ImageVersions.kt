package global.image

import jakarta.persistence.Column
import jakarta.persistence.Embeddable
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated

@Embeddable
class ImageVersions(
    @Column(name = "raw_url", length = 2048)
    val rawUrl: String = "",

    @Column(name = "public_url", length = 2048)
    val publicUrl: String = "",

    @Column(name = "thumbnail_url", length = 2048)
    val thumbnailUrl: String = "",

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    val status: ImageStatus = ImageStatus.PROCESSING,

    @Column(name = "aspect_ratio")
    val aspectRatio: Double = 1.0,
) {
    fun complete(
        publicUrl: String,
        thumbnailUrl: String,
        aspectRatio: Double
    ): ImageVersions = ImageVersions(
        rawUrl = rawUrl,
        publicUrl = publicUrl.trim(),
        thumbnailUrl = thumbnailUrl.trim(),
        status = ImageStatus.SUCCESS,
        aspectRatio = aspectRatio,
    )

    fun fail(): ImageVersions = ImageVersions(
        rawUrl = rawUrl,
        publicUrl = publicUrl,
        thumbnailUrl = thumbnailUrl,
        status = ImageStatus.FAIL,
        aspectRatio = aspectRatio,
    )
}
