package nz.scuttlebutt.android_go.models

import androidx.paging.ItemKeyedDataSource
import com.apollographql.apollo.api.Response
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.patchql.PatchqlApollo

class ThreadsDataSource(private val patchqlApollo: PatchqlApollo) :
    ItemKeyedDataSource<String, Thread>() {

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<Thread>
    ) {

        val thread = loadThreadsBefore(params.requestedInitialKey, params.requestedLoadSize)

        callback.onResult(thread)
    }


    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Thread>) {
        val thread = loadThreadsAfter(params.key, params.requestedLoadSize)
        callback.onResult(thread)

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Thread>) {
        val thread = loadThreadsBefore(params.key, params.requestedLoadSize)
        callback.onResult(thread)
    }

    override fun getKey(item: Thread): String {
        return item.cursor
    }

    private fun loadThreadsBefore(
        cursor: String?,
        num: Int
    ): List<Thread> {
        val threadsQuery = ThreadsSummaryQuery
            .builder()
            .before(cursor)
            .last(num)
            .build()

        val result = patchqlApollo.query(threadsQuery)
        return responseIntoThreads(result)

    }

    private fun loadThreadsAfter(
        cursor: String?,
        num: Int
    ): List<Thread> {
        val threadsQuery = ThreadsSummaryQuery
            .builder()
            .after(cursor)
            .first(num)
            .build()

        val result = patchqlApollo.query(threadsQuery)
        return responseIntoThreads(result)
    }

    private fun responseIntoThreads(it: Response<*>): List<Thread> {
        val data = it.data() as ThreadsSummaryQuery.Data

        return data.threads().edges().map { edge ->
            val root = edge.node().root()
            val cursor = edge.cursor()
            val repliesLength = edge.node().replies().size

            val rootPost = Post(
                root.id(),
                root.text(),
                root.likesCount(),
                root.likedByMe(),
                root.author().name(),
                root.author().imageLink(),
                root.references().size,
                repliesLength,
                null
            )

            Thread(rootPost, cursor.orEmpty())
        }
    }

}
