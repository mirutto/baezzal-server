package server.feed.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "collection_post")
class FeedCollectionPost(
    @Id
    @Column(name = "collection_post_id", nullable = false)
    val id: Long = 0,

    @Column(name = "collection_id", nullable = false)
    val collectionId: Long = 0,

    @Column(name = "post_id", nullable = false)
    val postId: Long = 0,
)
