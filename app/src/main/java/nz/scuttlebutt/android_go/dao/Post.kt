package nz.scuttlebutt.android_go.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource

import nz.scuttlebutt.android_go.models.Post as PostModel

interface Post {
    fun reload(postId: String)
    fun getAllPaged(query: String): DataSource.Factory<String, LiveData<PostModel>>
    fun save(post: PostModel)
}