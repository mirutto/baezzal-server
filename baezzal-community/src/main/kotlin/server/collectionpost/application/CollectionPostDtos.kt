package server.collectionpost.application

data class AddCollectionPostCommand(
    val postId: Long,
)

data class CollectionPostResult(
    val collectionId: Long,
    val postId: Long,
)
