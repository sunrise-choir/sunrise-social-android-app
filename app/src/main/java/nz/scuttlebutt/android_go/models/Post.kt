package nz.scuttlebutt.android_go.models

data class Post(
    val id: String,
    val text: String,
    val likesCount: String,
    val likedByMe: Boolean,
    val authorName: String?,
    val authorImageLink: String?,
    val referencesLength: String
)