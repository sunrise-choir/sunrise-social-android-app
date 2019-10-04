package nz.scuttlebutt.android_go.models

import androidx.recyclerview.widget.DiffUtil

data class Post(
    val id: String,
    val text: String,
    val likesCount: String,
    val likedByMe: Boolean,
    val authorName: String?,
    val authorImageLink: String?,
    val referencesLength: String,
    val repliesCount: String?,
    val cursor: String?
){

    companion object {
        var DIFF_CALLBACK: DiffUtil.ItemCallback<Post> =
            object : DiffUtil.ItemCallback<Post>() {
                override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
                    return oldItem.id === newItem.id
                }

                override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
                    return oldItem.equals(newItem)
                }
            }
    }
}
