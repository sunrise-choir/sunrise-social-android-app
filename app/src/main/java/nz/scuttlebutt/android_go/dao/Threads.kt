package nz.scuttlebutt.android_go.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import nz.scuttlebutt.android_go.models.Thread

interface Threads {
    fun reload(rootId: String)
    fun getAllPaged(query: () -> ThreadsSummaryQuery.Builder): DataSource.Factory<String, LiveData<Thread>>
    fun like(postId: String, doesLike: Boolean)
}