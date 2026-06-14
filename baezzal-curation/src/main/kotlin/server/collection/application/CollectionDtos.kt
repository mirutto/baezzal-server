package server.collection.application

import global.image.ImageVersionsData
import server.collection.domain.Collection

data class CreateCollectionCommand(
    val name: String,
    val description: String,
    val isPublished: Boolean,
)

data class CollectionData(
    val collectionId: Long,
    val name: String,
    val description: String,
    val imageVersions: ImageVersionsData,
    val isCustomThumbnail: Boolean,
    val isPublished: Boolean,
) {
    constructor(collection: Collection) : this(
        collectionId = collection.id,
        name = collection.name,
        description = collection.description,
        imageVersions = ImageVersionsData(collection.imageVersions),
        isCustomThumbnail = collection.isCustomThumbnail,
        isPublished = collection.isPublished,
    )
}

data class CollectionDeleteResult(
    val collectionId: Long,
)
