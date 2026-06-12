package server.collection.application

import global.error.BadRequestException
import global.error.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.collection.domain.Collection
import server.collection.implementation.CollectionEventPublisher
import server.collection.implementation.CollectionLocker
import server.collection.implementation.CollectionReader
import server.collection.implementation.CollectionRemover
import server.collection.implementation.CollectionWriter
import server.collectionpost.domain.CollectionPost
import server.collectionpost.implementation.CollectionPostLocker
import server.collectionpost.implementation.CollectionPostReader
import server.collectionpost.implementation.CollectionPostRemover
import server.collectionpost.implementation.CollectionPostWriter
import global.image.ImageVersions
import server.post.domain.Post
import server.post.implementation.PostReader

class CollectionServiceTest {
    private val collectionWriter = mockk<CollectionWriter>()
    private val collectionReader = mockk<CollectionReader>()
    private val collectionRemover = mockk<CollectionRemover>()
    private val collectionLocker = mockk<CollectionLocker>()
    private val collectionEventPublisher = mockk<CollectionEventPublisher>()
    private val postReader = mockk<PostReader>()
    private val collectionPostReader = mockk<CollectionPostReader>()
    private val collectionPostWriter = mockk<CollectionPostWriter>()
    private val collectionPostRemover = mockk<CollectionPostRemover>()
    private val collectionPostLocker = mockk<CollectionPostLocker>()
    private val collectionService = CollectionService(
        collectionWriter = collectionWriter,
        collectionReader = collectionReader,
        collectionRemover = collectionRemover,
        collectionLocker = collectionLocker,
        collectionEventPublisher = collectionEventPublisher,
        postReader = postReader,
        collectionPostReader = collectionPostReader,
        collectionPostWriter = collectionPostWriter,
        collectionPostRemover = collectionPostRemover,
        collectionPostLocker = collectionPostLocker,
    )

    init {
        every { collectionLocker.withLock(any(), any<() -> Any>()) } answers {
            secondArg<() -> Any>().invoke()
        }
        every { collectionPostLocker.withLock(any(), any(), any<() -> Any>()) } answers {
            thirdArg<() -> Any>().invoke()
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
                thumbnailUrl = " https://cdn.example.com/collections/1.webp ",
            ),
        )

        result shouldBe CollectionData(
            collectionId = 1L,
            name = "직관 모음",
            thumbnailUrl = "https://cdn.example.com/collections/1.webp",
        )
        savedCollection.captured.memberId shouldBe 7L
        savedCollection.captured.name shouldBe "직관 모음"
        savedCollection.captured.thumbnailUrl shouldBe "https://cdn.example.com/collections/1.webp"
    }

    @Test
    fun `member 의 collection 전체를 조회한다`() {
        every { collectionReader.readAllByMemberId(7L) } returns listOf(
            Collection(
                id = 3L,
                memberId = 7L,
                name = "직관 모음",
                thumbnailUrl = "https://cdn.example.com/collections/3.webp",
            ),
            Collection(
                id = 1L,
                memberId = 7L,
                name = "맛집 모음",
                thumbnailUrl = "https://cdn.example.com/collections/1.webp",
            ),
        )

        val result = collectionService.findAllByMemberId(7L)

        result shouldBe listOf(
            CollectionData(
                collectionId = 3L,
                name = "직관 모음",
                thumbnailUrl = "https://cdn.example.com/collections/3.webp",
            ),
            CollectionData(
                collectionId = 1L,
                name = "맛집 모음",
                thumbnailUrl = "https://cdn.example.com/collections/1.webp",
            ),
        )
    }

    @Test
    fun `collection 을 수정한다`() {
        val collection = collection()
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns collection

        val result = collectionService.update(
            memberId = 7L,
            collectionId = 1L,
            command = UpdateCollectionCommand(
                name = " 맛집 모음 ",
                thumbnailUrl = " https://cdn.example.com/collections/updated.webp ",
            ),
        )

        result shouldBe CollectionData(
            collectionId = 1L,
            name = "맛집 모음",
            thumbnailUrl = "https://cdn.example.com/collections/updated.webp",
        )
        collection.name shouldBe "맛집 모음"
        collection.thumbnailUrl shouldBe "https://cdn.example.com/collections/updated.webp"
        verify(exactly = 1) { collectionLocker.withLock(1L, any<() -> CollectionData>()) }
    }

    @Test
    fun `collection 에 post 를 추가한다`() {
        val savedCollectionPost = slot<CollectionPost>()
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns collection()
        every { postReader.readById(10L) } returns Post(
            id = 10L,
            memberId = 3L,
            image = ImageVersions(rawUrl = "https://cdn.example.com/posts/10.png"),
        )
        every { collectionPostReader.exists(1L, 10L) } returns false
        every { collectionPostWriter.write(capture(savedCollectionPost)) } answers { firstArg() }
        every { collectionEventPublisher.publishPostAdded(7L, 1L, 10L) } returns Unit

        val result = collectionService.addPost(
            memberId = 7L,
            collectionId = 1L,
            command = AddCollectionPostCommand(postId = 10L),
        )

        result shouldBe CollectionPostResult(
            collectionId = 1L,
            postId = 10L,
        )
        savedCollectionPost.captured.collectionId shouldBe 1L
        savedCollectionPost.captured.postId shouldBe 10L
        verify(exactly = 1) { collectionLocker.withLock(1L, any<() -> CollectionPostResult>()) }
        verify(exactly = 1) { collectionPostLocker.withLock(1L, 10L, any<() -> CollectionPostResult>()) }
        verify(exactly = 1) { collectionEventPublisher.publishPostAdded(7L, 1L, 10L) }
    }

    @Test
    fun `이미 추가된 post 는 중복 추가할 수 없다`() {
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns collection()
        every { postReader.readById(10L) } returns Post(
            id = 10L,
            memberId = 3L,
            image = ImageVersions(rawUrl = "https://cdn.example.com/posts/10.png"),
        )
        every { collectionPostReader.exists(1L, 10L) } returns true

        shouldThrow<BadRequestException> {
            collectionService.addPost(
                memberId = 7L,
                collectionId = 1L,
                command = AddCollectionPostCommand(postId = 10L),
            )
        }
    }

    @Test
    fun `존재하지 않는 post 는 추가할 수 없다`() {
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns collection()
        every { postReader.readById(99L) } returns null

        shouldThrow<NotFoundException> {
            collectionService.addPost(
                memberId = 7L,
                collectionId = 1L,
                command = AddCollectionPostCommand(postId = 99L),
            )
        }
    }

    @Test
    fun `내 컬렉션이 아니면 post 를 추가할 수 없다`() {
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns null

        shouldThrow<NotFoundException> {
            collectionService.addPost(
                memberId = 7L,
                collectionId = 1L,
                command = AddCollectionPostCommand(postId = 10L),
            )
        }
    }

    @Test
    fun `collection 에서 post 를 제거한다`() {
        val collectionPost = CollectionPost(
            id = 11L,
            collectionId = 1L,
            postId = 10L,
        )
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns collection()
        every { collectionPostReader.readByCollectionIdAndPostId(1L, 10L) } returns collectionPost
        every { collectionPostRemover.remove(collectionPost) } returns Unit

        val result = collectionService.removePost(
            memberId = 7L,
            collectionId = 1L,
            postId = 10L,
        )

        result shouldBe CollectionPostResult(
            collectionId = 1L,
            postId = 10L,
        )
        verify(exactly = 1) { collectionLocker.withLock(1L, any<() -> CollectionPostResult>()) }
        verify(exactly = 1) { collectionPostRemover.remove(collectionPost) }
        verify(exactly = 1) { collectionPostLocker.withLock(1L, 10L, any<() -> CollectionPostResult>()) }
    }

    @Test
    fun `collection 에 없는 post 는 제거할 수 없다`() {
        every { collectionReader.readByIdAndMemberId(1L, 7L) } returns collection()
        every { collectionPostReader.readByCollectionIdAndPostId(1L, 10L) } returns null

        shouldThrow<BadRequestException> {
            collectionService.removePost(
                memberId = 7L,
                collectionId = 1L,
                postId = 10L,
            )
        }
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

    private fun collection(memberId: Long = 7L): Collection = Collection(
        id = 1L,
        memberId = memberId,
        name = "직관 모음",
        thumbnailUrl = "https://cdn.example.com/collections/1.webp",
    )

    private fun Collection.copyForTest(id: Long): Collection = Collection(
        id = id,
        memberId = memberId,
        name = name,
        thumbnailUrl = thumbnailUrl,
    )
}
