package server.member.domain

enum class MemberProvider {
    GOOGLE, KAKAO, NAVER;

    companion object {
        fun from(providerName: String): MemberProvider =
            entries.firstOrNull { it.name.equals(providerName, ignoreCase = true) }
                ?: throw IllegalStateException("Provider $providerName not found")
    }
}
