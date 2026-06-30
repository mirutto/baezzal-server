package server.feed.model.tagrelation

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "tag_relation")
class FeedTagRelation(
    @Id
    @Column(name = "tag_relation_id", nullable = false)
    val id: Long = 0,

    @Column(name = "source_tag_id", nullable = false)
    val sourceTagId: Long = 0,

    @Column(name = "target_tag_id", nullable = false)
    val targetTagId: Long = 0,

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    val relationType: FeedTagRelationType = FeedTagRelationType.CO_OCCURRENCE,

    @Column(name = "score", nullable = false)
    val score: Long = 0,

    @Column(
        name = "created_at",
        nullable = false,
        updatable = false,
        columnDefinition = "DATETIME(6)",
    )
    val createdAt: LocalDateTime? = null,

    @Column(
        name = "updated_at",
        nullable = false,
        columnDefinition = "DATETIME(6)",
    )
    val updatedAt: LocalDateTime? = null,
)
