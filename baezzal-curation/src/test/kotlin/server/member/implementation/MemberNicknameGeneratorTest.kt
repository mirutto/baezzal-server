package server.member.implementation

import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import server.team.domain.TeamIds

class MemberNicknameGeneratorTest {
    private val memberNicknameGenerator = MemberNicknameGenerator()

    @Test
    fun `팀 아이디에 맞는 별칭으로 랜덤 닉네임을 생성한다`() {
        aliasesByTeamId.forEach { (teamId, alias) ->
            val nickname = memberNicknameGenerator.generateRandomNickname(teamId)
            val parts = nickname.split(" ")

            parts.size shouldBe 2
            prefixes.shouldContain(parts[0])
            parts[1] shouldBe alias
        }
    }

    companion object {
        private val aliasesByTeamId = mapOf(
            TeamIds.LG to "쌍둥이",
            TeamIds.HANWHA to "독수리",
            TeamIds.SSG to "강아지",
            TeamIds.SAMSUNG to "사자",
            TeamIds.NC to "공룡",
            TeamIds.KT to "마법사",
            TeamIds.LOTTE to "거인",
            TeamIds.KIA to "호랑이",
            TeamIds.DOOSAN to "곰",
            TeamIds.KIWOOM to "히어로",
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
