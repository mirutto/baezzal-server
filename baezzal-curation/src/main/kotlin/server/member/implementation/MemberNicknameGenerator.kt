package server.member.implementation

import org.springframework.stereotype.Component
import server.team.domain.TeamIds

@Component
class MemberNicknameGenerator {

    fun generateRandomNickname(teamId: Long): String {
        return "${randomPrefix()} ${teamAlias(teamId)}"
    }

    private fun randomPrefix(): String = listOf(
        "홈런왕", "득점왕", "도루왕", "탈삼진왕", "슬러거",
        "타격왕", "안타왕", "다승왕", "세이브왕", "홀드왕",
        "방어율왕", "MVP", "골든글러브", "신잉왕", "거포",
        "강타자", "리드오프", "중심타자", "클러치히터", "교타자",
        "천재타자", "출루기계", "홈런타자", "에이스", "선발투수",
        "구원투수", "마무리", "클로저", "파이어볼러", "제구마스터",
        "승리요정", "해결사", "호수비", "4번타자", "평범한"
    ).random()

    private fun teamAlias(teamId: Long): String = when(teamId) {
        TeamIds.LG -> "쌍둥이"
        TeamIds.HANWHA -> "독수리"
        TeamIds.SSG -> "강아지"
        TeamIds.SAMSUNG -> "사자"
        TeamIds.NC -> "공룡"
        TeamIds.KT -> "마법사"
        TeamIds.LOTTE -> "거인"
        TeamIds.KIA -> "호랑이"
        TeamIds.DOOSAN -> "곰"
        TeamIds.KIWOOM -> "히어로"
        else -> ""
    }
}
