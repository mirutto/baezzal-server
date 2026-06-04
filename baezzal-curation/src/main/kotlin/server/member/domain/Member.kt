package server.member.domain

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
    name = "member",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_provider_provider_key",
            columnNames = ["provider", "provider_key"],
        ),
    ],
)
class Member(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    val id: Long = 0,

    @Column(name = "nickname", nullable = false, length = 50)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    val provider: MemberProvider,

    @Column(name = "provider_key", nullable = false, length = 255)
    val providerKey: String,

    @Column(name = "profile_image", nullable = false, length = 500)
    var profileImage: String,

    @Column(name = "preferred_team_id")
    var preferredTeamId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    val role: MemberRole = MemberRole.USER,
) : BaseEntity() {
    companion object {
        const val DEFAULT_PROFILE_IMAGE_URL = "https://static.wowan.me/baezzal/images/mirutto_default.png"
    }

    fun isNew(): Boolean = nickname.isBlank() || preferredTeamId == null

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun updatePreferredTeam(preferredTeamId: Long?) {
        this.preferredTeamId = preferredTeamId
    }

    fun updateProfileImage(profileImage: String) {
        this.profileImage = profileImage
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
