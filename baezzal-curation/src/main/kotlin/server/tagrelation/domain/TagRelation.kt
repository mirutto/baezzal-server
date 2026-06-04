package server.tagrelation.domain

import global.entity.BaseEntity
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint

@Entity
@Table(
    name = "tag_relation",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_tag_relation_source_target_type",
            columnNames = ["source_tag_id", "target_tag_id", "relation_type"],
        ),
    ],
)
class TagRelation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tag_relation_id", nullable = false)
    val id: Long = 0,

    @Column(name = "source_tag_id", nullable = false)
    val sourceTagId: Long,

    @Column(name = "target_tag_id", nullable = false)
    val targetTagId: Long,

    @Enumerated(EnumType.STRING)
    @Column(name = "relation_type", nullable = false, length = 30)
    val relationType: TagRelationType,

    @Column(name = "score", nullable = false)
    var score: Long = 0,
) : BaseEntity() {
    init {
        require(sourceTagId < targetTagId) { "sourceTagId 는 targetTagId 보다 작아야 합니다" }
    }

    fun increaseScore(delta: Long) {
        score += delta
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is TagRelation) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
