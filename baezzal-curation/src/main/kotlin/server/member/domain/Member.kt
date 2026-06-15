package server.member.domain

import global.entity.BaseEntity
import global.image.ImageStatus
import global.image.ImageVersions
import global.uuid.isUuid
import jakarta.persistence.AttributeOverride
import jakarta.persistence.AttributeOverrides
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import java.util.UUID

@Entity
@Table(
    name = "member",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_member_username",
            columnNames = ["username"],
        ),
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

    @Column(name = "username", nullable = false, length = 36)
    var username: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 20)
    val provider: MemberProvider,

    @Column(name = "provider_key", nullable = false, length = 255)
    val providerKey: String,

    @Embedded
    @AttributeOverrides(
        AttributeOverride(
            name = "rawUrl",
            column = Column(name = "profile_image", nullable = false, length = 2048),
        ),
        AttributeOverride(
            name = "publicUrl",
            column = Column(name = "public_profile_image", nullable = false, length = 2048),
        ),
        AttributeOverride(
            name = "thumbnailUrl",
            column = Column(name = "thumbnail_profile_image", nullable = false, length = 2048),
        ),
        AttributeOverride(
            name = "status",
            column = Column(name = "profile_image_status", nullable = false, length = 20),
        ),
        AttributeOverride(
            name = "aspectRatio",
            column = Column(name = "profile_image_aspect_ratio", nullable = false),
        ),
    )
    var profileImage: ImageVersions,

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    var description: String,

    @Column(name = "preferred_team_id")
    var preferredTeamId: Long? = null,

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    val role: MemberRole = MemberRole.USER,
) : BaseEntity() {
    companion object {
        private const val STATIC_DOMAIN = "https://static.wowan.me"
        const val DEFAULT_PROFILE_IMAGE_URL =
            "$STATIC_DOMAIN/baezzal/members/raw/mirutto_default_profile.png"
        const val DEFAULT_PROFILE_PUBLIC_URL =
            "$STATIC_DOMAIN/baezzal/members/public/mirutto_default_profile.png"
        const val DEFAULT_PROFILE_THUMBNAIL_URL =
            "$STATIC_DOMAIN/baezzal/members/thumbnail/mirutto_default_profile.webp"

        fun defaultProfileImage(): ImageVersions = ImageVersions(
            rawUrl = DEFAULT_PROFILE_IMAGE_URL,
            publicUrl = DEFAULT_PROFILE_PUBLIC_URL,
            thumbnailUrl = DEFAULT_PROFILE_THUMBNAIL_URL,
            status = ImageStatus.SUCCESS,
            aspectRatio = 1.0,
        )
    }

    fun isNew(): Boolean = nickname.isBlank() || preferredTeamId == null || username.isUuid()

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun updatePreferredTeam(preferredTeamId: Long?) {
        this.preferredTeamId = preferredTeamId
    }

    fun updateProfileImage(profileImage: String) {
        this.profileImage = ImageVersions(
            rawUrl = profileImage,
            publicUrl = profileImage,
            thumbnailUrl = profileImage,
            status = ImageStatus.SUCCESS,
            aspectRatio = 1.0,
        )
    }

    fun updateUsername(username: String) {
        this.username = username
    }

    fun updateDescription(description: String) {
        this.description = description
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Member) return false
        return id != 0L && id == other.id
    }

    override fun hashCode(): Int = javaClass.hashCode()
}
