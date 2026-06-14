package server.collection.application

import global.image.ImageStatus
import global.image.ImageVersionsData
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.collection.domain.Collection
import server.collection.implementation.CollectionLocker
import server.collection.implementation.CollectionReader
import server.collection.implementation.CollectionRemover
import server.collection.implementation.CollectionWriter
import server.collectionpost.implementation.CollectionPostRemover
import global.image.ImageVersions
import io.mockk.slot

class CollectionServiceTest {
    private val collectionWriter = mockk<CollectionWriter>()
    private val collectionReader = mockk<CollectionReader>()
    private val collectionRemover = mockk<CollectionRemover>()
    private val collectionLocker = mockk<CollectionLocker>()
    private val collectionPostRemover = mockk<CollectionPostRemover>()
    private val collectionService = CollectionService(
        collectionWriter = collectionWriter,
        collectionReader = collectionReader,
        collectionRemover = collectionRemover,
        collectionLocker = collectionLocker,
        collectionPostRemover = collectionPostRemover,
    )

    init {
        every { collectionLocker.withLock(any(), any<() -> Any>()) } answers {
            secondArg<() -> Any>().invoke()
        }
    }

    @Test
    fun `collection 을 생성한다`() {
        val savedCollection = slot<Collection>()
        every { collectionWriter.write(capture(savedCollection)) } answers {
            savedCollection.captured.copyForTest(id = 1L)
        }

        val result = collectionService.create(
            memberId = 7L,
            command = CreateCollectionCommand(
                name = " 직관 모음 ",
                description = " 직관 기록 모음 ",
                isPublished = false,
            ),
        )

        result shouldBe CollectionData(
            collectionId = 1L,
            name = "직관 모음",
            description = "직관 기록 모음",
            imageVersions = imageVersionsData(
                rawUrl = "",
                publicUrl = "",
                thumbnailUrl = "",
                status = "PROCESSING",
                aspectRatio = 1.0,
            ),
            isCustomThumbnail = false,
            isPublished = false,
        )
        savedCollection.captured.memberId shouldBe 7L
        savedCollection.captured.name shouldBe "직관 모음"
        savedCollection.captured.description shouldBe "직관 기록 모음"
        savedCollection.captured.imageVersions.rawUrl shouldBe ""
        savedCollection.captured.imageVersions.publicUrl shouldBe ""
        savedCollection.captured.imageVersions.thumbnailUrl shouldBe ""
        savedCollection.captured.imageVersions.status shouldBe ImageStatus.PROCESSING
        savedCollection.captured.imageVersions.aspectRatio shouldBe 1.0
        savedCollection.captured.isCustomThumbnail shouldBe false
        savedCollection.captured.isPublished shouldBe false
    }

    @Test
    fun `member 기본 collection 을 생성한다`() {
        val savedCollection = slot<Collection>()
        every { collectionWriter.write(capture(savedCollection)) } answers {
            savedCollection.captured.copyForTest(id = 2L)
        }

        val result = collectionService.createDefault(9L)

        result.id shouldBe 2L
        result.memberId shouldBe 9L
        result.name shouldBe "나중에 볼 짤북"
        result.description shouldBe ""
        result.imageVersions.rawUrl shouldBe ""
        result.imageVersions.publicUrl shouldBe ""
        result.imageVersions.thumbnailUrl shouldBe ""
        result.imageVersions.status shouldBe ImageStatus.PROCESSING
        result.imageVersions.aspectRatio shouldBe 1.0
        result.isCustomThumbnail shouldBe false
        result.isPublished shouldBe false
    }

    @Test
    fun `member 의 collection 전체를 조회한다`() {
        val collections = listOf(
            collection(
                id = 3L,
                name = "직관 모음",
                description = "직관 기록 모음",
                rawUrl = "https://cdn.example.com/collections/3-raw.webp",
                publicUrl = "https://cdn.example.com/collections/3-public.webp",
                thumbnailUrl = "https://cdn.example.com/collections/3-thumb.webp",
                status = ImageStatus.SUCCESS,
                aspectRatio = 1.2,
                isCustomThumbnail = true,
                isPublished = true,
            ),
            collection(
                id = 1L,
                name = "맛집 모음",
                description = "맛집 저장 모음",
                rawUrl = "https://cdn.example.com/collections/1-raw.webp",
                publicUrl = "https://cdn.example.com/collections/1-public.webp",
                thumbnailUrl = "https://cdn.example.com/collections/1-thumb.webp",
                status = ImageStatus.PROCESSING,
                aspectRatio = 1.0,
                isCustomThumbnail = false,
                isPublished = false,
            ),
        )
        every { collectionReader.readAllByMemberId(7L) } returns collections

        val result = collectionService.findAllByMemberId(7L)

        result shouldBe listOf(
            collectionData(
                id = 3L,
                name = "직관 모음",
                description = "직관 기록 모음",
                rawUrl = "https://cdn.example.com/collections/3-raw.webp",
                publicUrl = "https://cdn.example.com/collections/3-public.webp",
                thumbnailUrl = "https://cdn.example.com/collections/3-thumb.webp",
                status = "SUCCESS",
                aspectRatio = 1.2,
                isCustomThumbnail = true,
                isPublished = true,
            ),
            collectionData(
                id = 1L,
                name = "맛집 모음",
                description = "맛집 저장 모음",
                rawUrl = "https://cdn.example.com/collections/1-raw.webp",
                publicUrl = "https://cdn.example.com/collections/1-public.webp",
                thumbnailUrl = "https://cdn.example.com/collections/1-thumb.webp",
                status = "PROCESSING",
                aspectRatio = 1.0,
                isCustomThumbnail = false,
                isPublished = false,
            ),
        )
    }

    @Test
    fun `collection 을 삭제한다`() {
        val collection = collection()
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns collection
        every { collectionPostRemover.removeAllByCollectionId(1L) } returns Unit
        every { collectionRemover.remove(collection) } returns Unit

        val result = collectionService.delete(
            memberId = 7L,
            collectionId = 1L,
        )

        result shouldBe CollectionDeleteResult(collectionId = 1L)
        verify(exactly = 1) { collectionLocker.withLock(1L, any<() -> CollectionDeleteResult>()) }
        verify(exactly = 1) { collectionPostRemover.removeAllByCollectionId(1L) }
        verify(exactly = 1) { collectionRemover.remove(collection) }
    }

    private fun collection(
        id: Long = 1L,
        memberId: Long = 7L,
        name: String = "직관 모음",
        description: String = "직관 기록 모음",
        rawUrl: String = "https://cdn.example.com/collections/1-raw.webp",
        publicUrl: String = "https://cdn.example.com/collections/1-public.webp",
        thumbnailUrl: String = "https://cdn.example.com/collections/1-thumb.webp",
        status: ImageStatus = ImageStatus.SUCCESS,
        aspectRatio: Double = 1.5,
        isCustomThumbnail: Boolean = false,
        isPublished: Boolean = false,
    ): Collection = Collection(
        id = id,
        memberId = memberId,
        name = name,
        description = description,
        imageVersions = imageVersions(
            rawUrl = rawUrl,
            publicUrl = publicUrl,
            thumbnailUrl = thumbnailUrl,
            status = status,
            aspectRatio = aspectRatio,
        ),
        isCustomThumbnail = isCustomThumbnail,
        isPublished = isPublished,
    )

    private fun Collection.copyForTest(id: Long): Collection = Collection(
        id = id,
        memberId = memberId,
        name = name,
        description = description,
        imageVersions = imageVersions,
        isCustomThumbnail = isCustomThumbnail,
        isPublished = isPublished,
        lastPostRuleModifiedAt = lastPostRuleModifiedAt,
    )

    private fun imageVersions(
        rawUrl: String,
        publicUrl: String,
        thumbnailUrl: String,
        status: ImageStatus,
        aspectRatio: Double,
    ) = ImageVersions(
        rawUrl = rawUrl,
        publicUrl = publicUrl,
        thumbnailUrl = thumbnailUrl,
        status = status,
        aspectRatio = aspectRatio,
    )

    private fun imageVersionsData(
        rawUrl: String,
        publicUrl: String,
        thumbnailUrl: String,
        status: String,
        aspectRatio: Double,
    ) = ImageVersionsData(
        rawUrl = rawUrl,
        publicUrl = publicUrl,
        thumbnailUrl = thumbnailUrl,
        status = status,
        aspectRatio = aspectRatio,
    )

    private fun collectionData(
        id: Long,
        name: String,
        description: String,
        rawUrl: String,
        publicUrl: String,
        thumbnailUrl: String,
        status: String,
        aspectRatio: Double,
        isCustomThumbnail: Boolean,
        isPublished: Boolean,
    ) = CollectionData(
        collectionId = id,
        name = name,
        description = description,
        imageVersions = imageVersionsData(
            rawUrl = rawUrl,
            publicUrl = publicUrl,
            thumbnailUrl = thumbnailUrl,
            status = status,
            aspectRatio = aspectRatio,
        ),
        isCustomThumbnail = isCustomThumbnail,
        isPublished = isPublished,
    )
}
