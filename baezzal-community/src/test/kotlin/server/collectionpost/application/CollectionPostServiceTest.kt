package server.collectionpost.application

import global.error.BadRequestException
import global.error.NotFoundException
import global.image.ImageVersions
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
import server.collectionpost.domain.CollectionPost
import server.collectionpost.implementation.CollectionPostLocker
import server.collectionpost.implementation.CollectionPostReader
import server.collectionpost.implementation.CollectionPostRemover
import server.collectionpost.implementation.CollectionPostWriter
import server.post.domain.Post
import server.post.implementation.PostReader

class CollectionPostServiceTest {
    private val collectionReader = mockk<CollectionReader>()
    private val collectionLocker = mockk<CollectionLocker>()
    private val collectionEventPublisher = mockk<CollectionEventPublisher>()
    private val postReader = mockk<PostReader>()
    private val collectionPostReader = mockk<CollectionPostReader>()
    private val collectionPostWriter = mockk<CollectionPostWriter>()
    private val collectionPostRemover = mockk<CollectionPostRemover>()
    private val collectionPostLocker = mockk<CollectionPostLocker>()
    private val collectionPostService = CollectionPostService(
        collectionReader = collectionReader,
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

        val result = collectionPostService.add(
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
            collectionPostService.add(
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
            collectionPostService.add(
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
            collectionPostService.add(
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

        val result = collectionPostService.remove(
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
            collectionPostService.remove(
                memberId = 7L,
                collectionId = 1L,
                postId = 10L,
            )
        }
    }

    private fun collection(
        id: Long = 1L,
        memberId: Long = 7L,
        name: String = "직관 모음",
        description: String = "직관 기록 모음",
    ): Collection = Collection(
        id = id,
        memberId = memberId,
        name = name,
        description = description,
        imageVersions = ImageVersions(rawUrl = "https://cdn.example.com/collections/1-raw.webp"),
        isCustomThumbnail = false,
        isPublished = false,
    )
}
