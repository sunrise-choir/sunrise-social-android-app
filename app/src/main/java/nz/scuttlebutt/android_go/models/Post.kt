package nz.scuttlebutt.android_go.models

data class Post(
    val id: String,
    val text: String,
    val likesCount: Int,
    val authorName: String?,
    val authorImageLink: String?
)