package server.member.domain

import global.error.InternalServerErrorException

enum class MemberProvider {
    GOOGLE, KAKAO, NAVER;

    companion object {
        fun from(providerName: String): MemberProvider =
            entries.firstOrNull { it.name.equals(providerName, ignoreCase = true) }
                ?: throw InternalServerErrorException("지원하지 않는 회원 provider 입니다")
    }
}
