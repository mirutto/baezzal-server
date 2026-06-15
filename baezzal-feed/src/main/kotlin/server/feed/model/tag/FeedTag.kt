package server.feed.model.tag

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "tag")
class FeedTag(
    @Id
    @Column(name = "tag_id", nullable = false)
    val id: Long = 0,

    @Column(name = "title", nullable = false, length = 100)
    val title: String = "",
)
