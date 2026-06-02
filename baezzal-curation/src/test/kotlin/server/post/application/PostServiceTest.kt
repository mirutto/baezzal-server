package server.post.application

import global.error.BadRequestException
import global.error.NotFoundException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Test
import server.post.domain.ImageAsset
import server.post.domain.Post
import server.post.domain.ThumbnailStatus
import server.post.implementation.PostEventPublisher
import server.post.implementation.PostReader
import server.post.implementation.PostValidator
import server.post.implementation.PostWriter
import server.posttag.implementation.PostTagWriter
import server.tag.domain.Tag
import server.tag.implementation.TagResolver

class PostServiceTest {
    private val postValidator = mockk<PostValidator>()
    private val postReader = mockk<PostReader>()
    private val postWriter = mockk<PostWriter>()
    private val postTagWriter = mockk<PostTagWriter>()
    private val tagResolver = mockk<TagResolver>()
    private val postEventPublisher = mockk<PostEventPublisher>()
    private val postService = PostService(
        postValidator = postValidator,
        postReader = postReader,
        postWriter = postWriter,
        postTagWriter = postTagWriter,
        tagResolver = tagResolver,
        postEventPublisher = postEventPublisher,
    )

    private val originalImageUrl = "https://cdn.example.com/post.png"
    private val thumbnailUrl = "https://static.wowan.me/thumbnails/post.webp"

    @Test
    fun `post 를 생성한다`() {
        val savedPost = slot<Post>()
        val resolvedTags = listOf(
            Tag(id = 10L, title = "KBO"),
            Tag(id = 11L, title = "잠실"),
        )

        every { postValidator.validateImageUrl(originalImageUrl) } returns Unit
        every { postValidator.normalizeTeamId(2L) } returns 2L
        every { postWriter.write(capture(savedPost)) } returns createdPost(
            id = 100L,
            memberId = 7L,
            teamId = 2L,
            width = 1080,
            height = 1350,
            aspectRatio = 1080.0 / 1350.0,
        )
        every { tagResolver.resolveAll(listOf("KBO", "잠실", "KBO")) } returns resolvedTags
        every { postTagWriter.writeAll(100L, resolvedTags) } returns emptyList()
        every { postEventPublisher.publishCreated(any()) } returns Unit

        val result = postService.create(
            memberId = 7L,
            command = createPostCommand(
                imageUrl = " $originalImageUrl ",
                imageWidth = 1080,
                imageHeight = 1350,
                imageAspectRatio = 1080.0 / 1350.0,
                description = " 경기 후기 ",
                teamId = 2L,
                tagTitles = listOf("KBO", "잠실", "KBO"),
            ),
        )

        result shouldBe createPostResult(
            postId = 100L,
            memberId = 7L,
            teamId = 2L,
            originalWidth = 1080,
            originalHeight = 1350,
            originalAspectRatio = 1080.0 / 1350.0,
            tagTitles = listOf("KBO", "잠실"),
        )
        savedPost.captured.memberId shouldBe 7L
        savedPost.captured.teamId shouldBe 2L
        savedPost.captured.description shouldBe "경기 후기"
        savedPost.captured.originalImage.url shouldBe originalImageUrl
        savedPost.captured.originalImage.width shouldBe 1080
        savedPost.captured.originalImage.height shouldBe 1350
        savedPost.captured.originalImage.aspectRatio shouldBe 1080.0 / 1350.0
        savedPost.captured.thumbnailImage.url shouldBe ""
        verify(exactly = 1) {
            postEventPublisher.publishCreated(
                createdPost(100L, 7L, 2L, 1080, 1350, 1080.0 / 1350.0),
            )
        }
    }

    @Test
    fun `team id 가 0 이면 null 로 저장한다`() {
        val savedPost = slot<Post>()

        every { postValidator.validateImageUrl(originalImageUrl) } returns Unit
        every { postValidator.normalizeTeamId(0L) } returns null
        every { postWriter.write(capture(savedPost)) } returns createdPost(
            id = 101L,
            memberId = 9L,
            teamId = null,
            width = 1200,
            height = 900,
            aspectRatio = 1200.0 / 900.0,
            description = "",
        )
        every { tagResolver.resolveAll(emptyList()) } returns emptyList()
        every { postTagWriter.writeAll(101L, emptyList()) } returns emptyList()
        every { postEventPublisher.publishCreated(any()) } returns Unit

        val result = postService.create(
            memberId = 9L,
            command = createPostCommand(
                imageUrl = originalImageUrl,
                imageWidth = 1200,
                imageHeight = 900,
                imageAspectRatio = 1200.0 / 900.0,
                teamId = 0L,
            ),
        )

        result.teamId shouldBe null
        result.memberId shouldBe 9L
        result.originalImage shouldBe imageAssetData(
            url = originalImageUrl,
            width = 1200,
            height = 900,
            aspectRatio = 1200.0 / 900.0,
        )
        savedPost.captured.memberId shouldBe 9L
        savedPost.captured.teamId shouldBe null
        verify(exactly = 1) {
            postEventPublisher.publishCreated(
                createdPost(101L, 9L, null, 1200, 900, 1200.0 / 900.0, ""),
            )
        }
    }

    @Test
    fun `존재하지 않는 team id 이면 예외가 발생한다`() {
        every { postValidator.validateImageUrl(originalImageUrl) } returns Unit
        every { postValidator.normalizeTeamId(99L) } throws NotFoundException("팀을 찾을 수 없습니다")

        shouldThrow<NotFoundException> {
            postService.create(
                memberId = 3L,
                command = CreatePostCommand(
                    imageUrl = originalImageUrl,
                    imageWidth = 1080,
                    imageHeight = 1350,
                    imageAspectRatio = 1080.0 / 1350.0,
                    teamId = 99L,
                ),
            )
        }
    }

    @Test
    fun `thumbnail 을 업데이트한다`() {
        val post = Post(
            id = 100L,
            memberId = 7L,
            originalImage = ImageAsset(url = originalImageUrl),
        )
        every { postReader.readById(100L) } returns post

        postService.updateThumbnail(thumbnailUpdatedEvent(postId = 100L, trimWrapped = true))

        post.originalImage.url shouldBe originalImageUrl
        post.originalImage.width shouldBe 1280
        post.originalImage.height shouldBe 720
        post.thumbnailUrl shouldBe thumbnailUrl
        post.thumbnailImage.width shouldBe 320
        post.thumbnailImage.height shouldBe 180
        post.thumbnailStatus shouldBe ThumbnailStatus.SUCCESS
    }

    @Test
    fun `thumbnail 업데이트 대상 post 가 없으면 예외가 발생한다`() {
        every { postReader.readById(999L) } returns null

        shouldThrow<NotFoundException> {
            postService.updateThumbnail(thumbnailUpdatedEvent(postId = 999L))
        }
    }

    @Test
    fun `image url 이 비어 있으면 예외가 발생한다`() {
        every { postValidator.validateImageUrl("") } throws BadRequestException("imageUrl 은 비어 있을 수 없습니다")

        shouldThrow<BadRequestException> {
            postService.create(
                memberId = 5L,
                command = CreatePostCommand(
                    imageUrl = "   ",
                    imageWidth = 1080,
                    imageHeight = 1350,
                    imageAspectRatio = 1080.0 / 1350.0,
                ),
            )
        }

        verify(exactly = 0) { postWriter.write(any()) }
        verify(exactly = 0) { tagResolver.resolveAll(any()) }
        verify(exactly = 0) { postTagWriter.writeAll(any<Long>(), any<List<Tag>>()) }
        verify(exactly = 0) { postEventPublisher.publishCreated(any()) }
    }

    private fun thumbnailUpdatedEvent(
        postId: Long,
        trimWrapped: Boolean = false,
    ): ThumbnailUpdatedEvent = ThumbnailUpdatedEvent(
        postId = postId,
        originalImage = imageAssetEvent(
            url = wrap(originalImageUrl, trimWrapped),
            width = 1280,
            height = 720,
            aspectRatio = 1280.0 / 720.0,
        ),
        thumbnailImage = imageAssetEvent(
            url = wrap(thumbnailUrl, trimWrapped),
            width = 320,
            height = 180,
            aspectRatio = 320.0 / 180.0,
        ),
    )

    private fun imageAssetEvent(
        url: String,
        width: Int?,
        height: Int?,
        aspectRatio: Double?,
    ): ImageAssetEvent = ImageAssetEvent(
        url = url,
        width = width,
        height = height,
        aspectRatio = aspectRatio,
    )

    private fun imageAssetData(
        url: String,
        width: Int? = null,
        height: Int? = null,
        aspectRatio: Double? = null,
    ): ImageAssetData = ImageAssetData(
        url = url,
        width = width,
        height = height,
        aspectRatio = aspectRatio,
    )

    private fun wrap(value: String, trimWrapped: Boolean): String =
        if (trimWrapped) " $value " else value

    private fun createPostCommand(
        imageUrl: String,
        imageWidth: Int = 1080,
        imageHeight: Int = 1350,
        imageAspectRatio: Double = 1080.0 / 1350.0,
        description: String = "",
        teamId: Long? = null,
        tagTitles: List<String> = emptyList(),
    ): CreatePostCommand = CreatePostCommand(
        imageUrl = imageUrl,
        imageWidth = imageWidth,
        imageHeight = imageHeight,
        imageAspectRatio = imageAspectRatio,
        description = description,
        teamId = teamId,
        tagTitles = tagTitles,
    )

    private fun createdPost(
        id: Long,
        memberId: Long,
        teamId: Long?,
        width: Int?,
        height: Int?,
        aspectRatio: Double?,
        description: String = "경기 후기",
    ): Post = Post(
        id = id,
        memberId = memberId,
        originalImage = ImageAsset(
            url = originalImageUrl,
            width = width,
            height = height,
            aspectRatio = aspectRatio,
        ),
        description = description,
        teamId = teamId,
    )

    private fun createPostResult(
        postId: Long,
        memberId: Long,
        teamId: Long?,
        originalWidth: Int?,
        originalHeight: Int?,
        originalAspectRatio: Double?,
        tagTitles: List<String>,
        description: String = "경기 후기",
    ): CreatePostResult = CreatePostResult(
        postId = postId,
        memberId = memberId,
        imageUrl = originalImageUrl,
        originalImage = imageAssetData(
            url = originalImageUrl,
            width = originalWidth,
            height = originalHeight,
            aspectRatio = originalAspectRatio,
        ),
        thumbnailUrl = "",
        thumbnailImage = imageAssetData(url = ""),
        thumbnailStatus = "PENDING",
        description = description,
        teamId = teamId,
        tagTitles = tagTitles,
    )

    private fun postData(
        postId: Long,
        memberId: Long,
        teamId: Long?,
        description: String,
        tagTitles: List<String>,
        originalWidth: Int? = null,
        originalHeight: Int? = null,
        originalAspectRatio: Double? = null,
    ): PostData = PostData(
        postId = postId,
        memberId = memberId,
        imageUrl = originalImageUrl,
        originalImage = imageAssetData(
            url = originalImageUrl,
            width = originalWidth,
            height = originalHeight,
            aspectRatio = originalAspectRatio,
        ),
        thumbnailUrl = "",
        thumbnailImage = imageAssetData(url = ""),
        thumbnailStatus = "PENDING",
        description = description,
        teamId = teamId,
        tagTitles = tagTitles,
    )
}
