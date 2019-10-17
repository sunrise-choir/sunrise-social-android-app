package nz.scuttlebutt.android_go.models

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil
import com.sunrisechoir.graphql.AuthorProfileQuery
import com.sunrisechoir.graphql.type.ContactState


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
    val blockerCount: Int,
    val relationshipToMe: ContactState,
    val relationshipToThem: ContactState

) {
    companion object {
        fun fromAuthorProfile(profile: AuthorProfileQuery.Data): Author {
            val author = profile.author()!!

            return Author(
                id = author.id(),
                name = author.name(),
                description = author.description(),
                followerCount = author.follows().size,
                followingCount = author.followedBy().size,
                blockerCount = author.blockedBy().size,
                blockingCount = author.blocks().size,
                relationshipToMe = author.contactStatusTo().public_(),
                relationshipToThem = author.contactStatusFrom().public_()
            )
        }
    }
}