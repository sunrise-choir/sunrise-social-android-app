package nz.scuttlebutt.android_go.models

import androidx.paging.ItemKeyedDataSource
import androidx.paging.PageKeyedDataSource
import com.apollographql.apollo.api.Response
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.patchql.PatchqlApollo

class ThreadsDataSource(private val patchqlApollo: PatchqlApollo) :
    PageKeyedDataSource<String, Thread>() {

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<String, Thread>
    ) {


        val pair = loadThreadsBefore(null, params.requestedLoadSize)
        callback.onResult(pair.second, null, pair.second.last().cursor)
    }


    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<String, Thread>) {
        val pair = loadThreadsAfter(params.key, params.requestedLoadSize)
        callback.onResult(pair.second, pair.second.first().cursor)

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<String, Thread>) {
        val pair = loadThreadsBefore(params.key, params.requestedLoadSize)
        callback.onResult(pair.second, pair.second.last().cursor)
    }

//    override fun getKey(item: Thread): String {
//        return item.cursor
//    }

    private fun loadThreadsBefore(
        cursor: String?,
        num: Int
    ): Pair<String?,List<Thread>> {
        val threadsQuery = ThreadsSummaryQuery
            .builder()
            .before(cursor)
            .last(num)
            .build()

        val result = patchqlApollo.query(threadsQuery)
        return Pair(cursor, responseIntoThreads(result))

    }

    private fun loadThreadsAfter(
        cursor: String?,
        num: Int
    ): Pair<String?, List<Thread>> {
        val threadsQuery = ThreadsSummaryQuery
            .builder()
            .after(cursor)
            .first(num)
            .build()

        val result = patchqlApollo.query(threadsQuery)
        return Pair(cursor,responseIntoThreads(result))
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
                root.likesCount().toString(),
                root.likedByMe(),
                root.author().name(),
                root.author().imageLink(),
                root.references().size.toString(),
                repliesLength.toString(),
                null
            )

            Thread(rootPost, cursor.orEmpty())
        }
    }
}