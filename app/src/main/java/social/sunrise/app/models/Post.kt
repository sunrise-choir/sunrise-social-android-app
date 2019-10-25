package social.sunrise.app.models

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil

typealias LivePost = LiveData<Post>

val LIVE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<LivePost>() {
    override fun areItemsTheSame(
        oldItem: LivePost,
        newItem: LivePost
    ): Boolean {
        return oldItem.value?.id === newItem.value?.id
    }

    override fun areContentsTheSame(
        oldItem: LivePost,
        newItem: LivePost
    ): Boolean {
        return oldItem.equals(newItem)
    }
}

data class Post(
    val id: String,
    val text: String,
    val likesCount: Int,
    val likedByMe: Boolean,
    val authorId: String,
    val authorName: String?,
    val authorImageLink: String?,
    val referencesLength: Int,
    val repliesCount: Int?,
    val cursor: String?,
    val assertedTime: Long?
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
