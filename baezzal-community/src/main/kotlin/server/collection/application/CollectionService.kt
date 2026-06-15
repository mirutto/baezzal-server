package server.collection.application

import global.error.BadRequestException
import global.error.NotFoundException
import global.image.ImageStatus
import global.image.ImageVersions
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import server.collection.domain.Collection
import server.collection.implementation.CollectionLocker
import server.collection.implementation.CollectionReader
import server.collection.implementation.CollectionRemover
import server.collection.implementation.CollectionWriter
import server.collectionpost.implementation.CollectionPostRemover

@Service
class CollectionService(
    private val collectionWriter: CollectionWriter,
    private val collectionReader: CollectionReader,
    private val collectionRemover: CollectionRemover,
    private val collectionLocker: CollectionLocker,
    private val collectionPostRemover: CollectionPostRemover,
) {
    @Transactional
    fun create(
        memberId: Long,
        command: CreateCollectionCommand,
    ): CollectionIdResult = CollectionIdResult(
        collectionWriter.write(
            Collection(
                memberId = memberId,
                name = command.name.trim(),
                description = command.description.trim(),
                imageVersions = ImageVersions(),
                isCustomThumbnail = false,
                isPublished = command.isPublished,
            ),
        ),
    )

    @Transactional
    fun createDefault(memberId: Long) = collectionWriter.write(
        Collection(
            memberId = memberId,
            name = DEFAULT_COLLECTION_NAME,
            description = DEFAULT_COLLECTION_DESCRIPTION,
            imageVersions = ImageVersions(),
            isCustomThumbnail = false,
            isPublished = false,
        ),
    )

    @Transactional(readOnly = true)
    fun findAllByMemberId(memberId: Long): List<CollectionData> =
        collectionReader.readAllByMemberId(memberId).map(::CollectionData)

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

    companion object {
        private const val DEFAULT_COLLECTION_NAME = "나중에 볼 짤북"
        private const val DEFAULT_COLLECTION_DESCRIPTION = ""
    }
}
