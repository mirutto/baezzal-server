package server.collection.application

import global.error.BadRequestException
import global.error.NotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.collection.domain.Collection
import server.collection.implementation.CollectionLocker
import server.collection.implementation.CollectionReader
import server.collection.implementation.CollectionRemover
import server.collection.implementation.CollectionEventPublisher
import server.collection.implementation.CollectionWriter
import server.collectionpost.domain.CollectionPost
import server.collectionpost.implementation.CollectionPostLocker
import server.collectionpost.implementation.CollectionPostReader
import server.collectionpost.implementation.CollectionPostRemover
import server.collectionpost.implementation.CollectionPostWriter
import server.post.implementation.PostReader

@Service
class CollectionService(
    private val collectionWriter: CollectionWriter,
    private val collectionReader: CollectionReader,
    private val collectionRemover: CollectionRemover,
    private val collectionLocker: CollectionLocker,
    private val collectionEventPublisher: CollectionEventPublisher,
    private val postReader: PostReader,
    private val collectionPostReader: CollectionPostReader,
    private val collectionPostWriter: CollectionPostWriter,
    private val collectionPostRemover: CollectionPostRemover,
    private val collectionPostLocker: CollectionPostLocker,
) {
    @Transactional
    fun create(
        memberId: Long,
        command: CreateCollectionCommand,
    ): CollectionData = CollectionData(
        collectionWriter.write(
            Collection(
                memberId = memberId,
                name = command.name.trim(),
                thumbnailUrl = command.thumbnailUrl.trim(),
            ),
        ),
    )

    @Transactional(readOnly = true)
    fun findAllByMemberId(memberId: Long): List<CollectionData> =
        collectionReader.readAllByMemberId(memberId).map(::CollectionData)

    @Transactional
    fun update(
        memberId: Long,
        collectionId: Long,
        command: UpdateCollectionCommand,
    ): CollectionData = collectionLocker.withLock(collectionId) {
        val collection = readOwnedCollection(collectionId, memberId)
        collection.update(
            name = command.name.trim(),
            thumbnailUrl = command.thumbnailUrl.trim(),
        )

        CollectionData(collection)
    }

    @Transactional
    fun addPost(
        memberId: Long,
        collectionId: Long,
        command: AddCollectionPostCommand,
    ): CollectionPostResult = collectionLocker.withLock(collectionId) {
        collectionPostLocker.withLock(collectionId, command.postId) {
            val collection = readOwnedCollection(collectionId, memberId)
            val postId = command.postId

            postReader.readById(postId)
                ?: throw NotFoundException("게시글을 찾을 수 없습니다")

            if (collectionPostReader.exists(collectionId, postId)) {
                throw BadRequestException("이미 컬렉션에 추가된 게시글입니다")
            }

            collectionPostWriter.write(
                CollectionPost(
                    collectionId = collection.id,
                    postId = postId,
                ),
            )
            collectionEventPublisher.publishPostAdded(
                userId = memberId,
                collectionId = collection.id,
                postId = postId,
            )

            CollectionPostResult(
                collectionId = collection.id,
                postId = postId,
            )
        }
    }

    @Transactional
    fun removePost(
        memberId: Long,
        collectionId: Long,
        postId: Long,
    ): CollectionPostResult = collectionLocker.withLock(collectionId) {
        collectionPostLocker.withLock(collectionId, postId) {
            val collection = readOwnedCollection(collectionId, memberId)
            val collectionPost = collectionPostReader.readByCollectionIdAndPostId(collectionId, postId)
                ?: throw BadRequestException("컬렉션에 없는 게시글입니다")

            collectionPostRemover.remove(collectionPost)

            CollectionPostResult(
                collectionId = collection.id,
                postId = collectionPost.postId,
            )
        }
    }

    @Transactional
    fun delete(
        memberId: Long,
        collectionId: Long,
    ): CollectionDeleteResult = collectionLocker.withLock(collectionId) {
        val collection = readOwnedCollection(collectionId, memberId)

        collectionPostRemover.removeAllByCollectionId(collection.id)
        collectionRemover.remove(collection)

        CollectionDeleteResult(collection.id)
    }

    private fun readOwnedCollection(
        collectionId: Long,
        memberId: Long,
    ) = collectionReader.readByIdAndMemberId(collectionId, memberId)
        ?: throw NotFoundException("컬렉션을 찾을 수 없습니다")
}
