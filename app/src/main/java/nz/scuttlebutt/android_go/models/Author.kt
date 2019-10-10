package nz.scuttlebutt.android_go.models

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil

typealias LiveAuthor = LiveData<Author>

val AUTHOR_LIVE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<LiveAuthor>() {
    override fun areItemsTheSame(
        oldItem: LiveAuthor,
        newItem: LiveAuthor
    ): Boolean {
        return oldItem.value?.id === newItem.value?.id
    }

    override fun areContentsTheSame(
        oldItem: LiveAuthor,
        newItem: LiveAuthor
    ): Boolean {
        return oldItem.equals(newItem)
    }
}

data class Author(
    val id: String,
    val name: String?,
    val description: String?,
    val followingCount: Int,
    val followerCount: Int,
    val blockingCount: Int,
    val blockerCount: Int
    //relationShipToMe:
)