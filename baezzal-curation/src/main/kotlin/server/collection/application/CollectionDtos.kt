package server.collection.application

import server.collection.domain.Collection

data class CreateCollectionCommand(
    val name: String,
    val thumbnailUrl: String,
)

data class UpdateCollectionCommand(
    val name: String,
    val thumbnailUrl: String,
)

data class AddCollectionPostCommand(
    val postId: Long,
)

data class CollectionData(
    val collectionId: Long,
    val name: String,
    val thumbnailUrl: String,
) {
    constructor(collection: Collection) : this(
        collectionId = collection.id,
        name = collection.name,
        thumbnailUrl = collection.thumbnailUrl,
    )
}

data class CollectionPostResult(
    val collectionId: Long,
    val postId: Long,
)

data class CollectionDeleteResult(
    val collectionId: Long,
)
