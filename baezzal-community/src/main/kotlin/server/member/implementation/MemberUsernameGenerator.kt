package server.member.implementation

import org.springframework.stereotype.Component
import server.team.domain.TeamCodes
import java.util.UUID

@Component
class MemberUsernameGenerator{

    fun generateRandomUsername(teamCode: String) =
        "${teamPrefix(teamCode)}-${personalPostfix()}"

    private fun teamPrefix(teamCode: String): String = when(teamCode) {
        TeamCodes.LG -> "lg"
        TeamCodes.HANWHA -> "hanwha"
        TeamCodes.SSG -> "ssg"
        TeamCodes.SAMSUNG -> "samsung"
        TeamCodes.NC -> "nc"
        TeamCodes.KT -> "kt"
        TeamCodes.LOTTE -> "lotte"
        TeamCodes.KIA -> "kia"
        TeamCodes.DOOSAN -> "doosan"
        TeamCodes.KIWOOM -> "kiwoom"
        else -> ""
    }

    private fun personalPostfix() =
        UUID.randomUUID().toString().split("-").first()
}
