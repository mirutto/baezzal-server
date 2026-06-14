package server.feed.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "team")
class FeedTeam(
    @Id
    @Column(name = "team_id", nullable = false)
    val id: Long = 0,

    @Column(name = "code", nullable = false, length = 50)
    val code: String = "",

    @Column(name = "name", nullable = false, length = 255)
    val name: String = "",

    @Column(name = "sort_order", nullable = false)
    val sortOrder: Int = 0,
)
