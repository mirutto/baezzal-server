package server.member.application

import server.member.domain.Member
import server.objectstorage.PresignedUploadUrl

data class MemberOnboardingCommand(
    val preferredTeamId: Long,
)

data class MemberNicknameUpdateCommand(
    val nickname: String,
)

data class MemberPreferredTeamUpdateCommand(
    val preferredTeamId: Long?,
)

data class MemberProfileImageUpdateCommand(
    val profileImage: String,
)

data class CreateMemberProfileImageUploadUrlCommand(
    val contentType: String,
)

data class MemberProfileImageUploadUrlResult(
    val objectKey: String,
    val uploadUrl: String,
    val fileUrl: String,
    val headers: Map<String, String>,
    val expiresInSeconds: Int,
) {
    companion object {
        fun from(uploadUrl: PresignedUploadUrl): MemberProfileImageUploadUrlResult =
            MemberProfileImageUploadUrlResult(
                objectKey = uploadUrl.objectKey,
                uploadUrl = uploadUrl.uploadUrl,
                fileUrl = uploadUrl.fileUrl,
                headers = uploadUrl.headers,
                expiresInSeconds = uploadUrl.expiresInSeconds,
            )
    }
}

data class MemberData(
    val nickname: String,
    val preferredTeamId: Long?,
    val profileImage: String,
) {
    constructor(member: Member) : this(
        nickname = member.nickname,
        preferredTeamId = member.preferredTeamId,
        profileImage = member.profileImage,
    )
}
