package nz.scuttlebutt.android_go.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.sunrisechoir.graphql.PostsQuery

import nz.scuttlebutt.android_go.models.Post as PostModel

interface Post {
    fun reload(postId: String)
    fun getAllPaged(query: () -> PostsQuery.Builder): DataSource.Factory<String, LiveData<PostModel>>
    fun like(postId: String, doesLike: Boolean)
}