package nz.scuttlebutt.android_go.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.apollographql.apollo.api.Operation
import nz.scuttlebutt.android_go.models.Post

interface Thread {
    fun reload(rootId: String)
    fun <D : Operation.Data> getAllPaged(query: Operation<D, *, *>): DataSource.Factory<String, LiveData<Post>>
    fun like(postId: String, doesLike: Boolean)
    fun reply(rootId: String, replyText: String, mentions: List<String>, channels: List<String>)
}