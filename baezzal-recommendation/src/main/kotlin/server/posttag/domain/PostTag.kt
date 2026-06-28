package server.posttag.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "post_tag")
class PostTag(
    @Id
    @Column(name = "post_tag_id", nullable = false)
    val id: Long = 0,

    @Column(name = "post_id", nullable = false)
    val postId: Long = 0,

    @Column(name = "tag_id", nullable = false)
    val tagId: Long = 0,
)
