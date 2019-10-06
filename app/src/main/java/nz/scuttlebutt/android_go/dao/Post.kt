package nz.scuttlebutt.android_go.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource

import nz.scuttlebutt.android_go.models.Post as PostModel

interface Post {
    fun load(postId: String): LiveData<PostModel>
    fun getAllPaged(query: String): DataSource.Factory<String, LiveData<PostModel>>
    fun save(post: PostModel)
}