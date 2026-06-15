package server.tagrelation.application

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.jupiter.api.Test
import server.posttag.domain.RecommendationPostTag
import server.posttag.implementation.RecommendationPostTagReader
import server.tagrelation.domain.TagRelation
import server.tagrelation.domain.TagRelationType
import server.tagrelation.implementation.TagRelationReader
import server.tagrelation.implementation.TagRelationWriter

class TagRelationServiceTest {
    private val recommendationPostTagReader = mockk<RecommendationPostTagReader>()
    private val tagRelationReader = mockk<TagRelationReader>()
    private val tagRelationWriter = mockk<TagRelationWriter>()
    private val tagRelationService = TagRelationService(
        recommendationPostTagReader = recommendationPostTagReader,
        tagRelationReader = tagRelationReader,
        tagRelationWriter = tagRelationWriter,
    )

    @Test
    fun `post 생성 시 태그 관계를 누적한다`() {
        val capturedRelations = slot<Collection<TagRelation>>()
        val existingRelation = TagRelation(
            id = 1L,
            sourceTagId = 10L,
            targetTagId = 20L,
            relationType = TagRelationType.CO_OCCURRENCE,
            score = 4,
        )

        every { recommendationPostTagReader.readAllByPostId(100L) } returns listOf(
            RecommendationPostTag(id = 1L, postId = 100L, tagId = 30L),
            RecommendationPostTag(id = 2L, postId = 100L, tagId = 10L),
            RecommendationPostTag(id = 3L, postId = 100L, tagId = 20L),
            RecommendationPostTag(id = 4L, postId = 100L, tagId = 20L),
        )
        every {
            tagRelationReader.readAllByRelationTypeAndPairs(
                relationType = TagRelationType.CO_OCCURRENCE,
                tagPairs = listOf(10L to 20L, 10L to 30L, 20L to 30L),
            )
        } returns listOf(existingRelation)
        every { tagRelationWriter.writeAll(capture(capturedRelations)) } answers {
            firstArg<List<TagRelation>>()
        }

        tagRelationService.recordPostCreated(
            PostCreatedEvent(
                postId = 100L,
                imageUrl = "https://cdn.example.com/post.png",
            ),
        )

        val relationsByPair = capturedRelations.captured.associateBy { it.sourceTagId to it.targetTagId }
        relationsByPair[10L to 20L]?.score shouldBe 5
        relationsByPair[10L to 30L]?.score shouldBe 1
        relationsByPair[20L to 30L]?.score shouldBe 1
    }

    @Test
    fun `태그가 하나 이하면 태그 관계를 저장하지 않는다`() {
        every { recommendationPostTagReader.readAllByPostId(101L) } returns listOf(
            RecommendationPostTag(id = 1L, postId = 101L, tagId = 10L),
        )

        tagRelationService.recordPostCreated(
            PostCreatedEvent(
                postId = 101L,
                imageUrl = "https://cdn.example.com/post.png",
            ),
        )

        io.mockk.verify(exactly = 0) {
            tagRelationReader.readAllByRelationTypeAndPairs(any(), any())
        }
        io.mockk.verify(exactly = 0) {
            tagRelationWriter.writeAll(any())
        }
    }
}
