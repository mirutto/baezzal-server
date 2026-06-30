package server.feed.query

data class TagRelationCandidateQueryRow(
    val tagId: Long,
    val title: String,
    val score: Long,
)
