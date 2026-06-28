package server.feed.model.member

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "member")
class FeedMember(
    @Id
    @Column(name = "member_id", nullable = false)
    val id: Long = 0,

    @Column(name = "nickname", nullable = false, length = 50)
    val nickname: String = "",

    @Column(name = "username", nullable = false, length = 36)
    val username: String = "",

    @Column(name = "thumbnail_profile_image", nullable = false, length = 2048)
    val thumbnailProfileImage: String = "",

    @Column(name = "preferred_team_id")
    val preferredTeamId: Long? = null,
)
