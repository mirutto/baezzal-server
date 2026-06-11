package server.member.implementation

import org.springframework.stereotype.Component
import server.team.domain.TeamIds
import java.util.UUID

@Component
class MemberUsernameGenerator{

    fun generateRandomUsername(teamId: Long) =
        "${teamPrefix(teamId)}-${personalPostfix()}"

    private fun teamPrefix(teamId: Long): String = when(teamId) {
        TeamIds.LG -> "lg"
        TeamIds.HANWHA -> "hanwha"
        TeamIds.SSG -> "ssg"
        TeamIds.SAMSUNG -> "samsung"
        TeamIds.NC -> "nc"
        TeamIds.KT -> "kt"
        TeamIds.LOTTE -> "lotte"
        TeamIds.KIA -> "kia"
        TeamIds.DOOSAN -> "doosan"
        TeamIds.KIWOOM -> "kiwoom"
        else -> ""
    }

    private fun personalPostfix() =
        UUID.randomUUID().toString().split("-").first()
}
