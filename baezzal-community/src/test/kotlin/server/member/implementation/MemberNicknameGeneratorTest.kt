package server.member.implementation

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import server.team.domain.TeamCodes

class MemberNicknameGeneratorTest {
    private val memberNicknameGenerator = MemberNicknameGenerator()

    @Test
    fun `팀 아이디에 맞는 별칭으로 랜덤 닉네임을 생성한다`() {
        aliasesByTeamId.forEach { (teamCode, alias) ->
            val nickname = memberNicknameGenerator.generateRandomNickname(teamCode)
            val parts = nickname.split(" ")

            parts.size shouldBe 2
            prefixes.shouldContain(parts[0])
            parts[1] shouldBe alias
        }
    }

    companion object {
        private val aliasesByTeamId = mapOf(
            TeamCodes.LG to "쌍둥이",
            TeamCodes.HANWHA to "독수리",
            TeamCodes.SSG to "강아지",
            TeamCodes.SAMSUNG to "사자",
            TeamCodes.NC to "공룡",
            TeamCodes.KT to "마법사",
            TeamCodes.LOTTE to "거인",
            TeamCodes.KIA to "호랑이",
            TeamCodes.DOOSAN to "곰",
            TeamCodes.KIWOOM to "히어로",
        )

        private val prefixes = listOf(
            "홈런왕", "득점왕", "도루왕", "탈삼진왕", "슬러거",
            "타격왕", "안타왕", "다승왕", "세이브왕", "홀드왕",
            "방어율왕", "MVP", "골든글러브", "신잉왕", "거포",
            "강타자", "리드오프", "중심타자", "클러치히터", "교타자",
            "천재타자", "출루기계", "홈런타자", "에이스", "선발투수",
            "구원투수", "마무리", "클로저", "파이어볼러", "제구마스터",
            "승리요정", "해결사", "호수비", "4번타자", "평범한",
        )
    }
}
