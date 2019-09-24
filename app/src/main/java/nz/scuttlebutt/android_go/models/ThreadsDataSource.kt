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
        loadThreadsBefore(params.requestedInitialKey, params.requestedLoadSize, callback)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Thread>) {
        loadThreadsAfter(params.key, params.requestedLoadSize, callback)

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Thread>) {
        loadThreadsBefore(params.key, params.requestedLoadSize, callback)
    }

    override fun getKey(item: Thread): String {
        return item.cursor
    }

    private fun loadThreadsBefore(
        cursor: String?,
        num: Int,
        callback: LoadCallback<Thread>
    ) {
        val threadsQuery = ThreadsSummaryQuery
            .builder()
            .before(cursor)
            .last(num)
            .build()

        patchqlApollo.query(threadsQuery) {
            it.map(::list)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql query failed. ${it}")
                }
        }
    }

    private fun loadThreadsAfter(
        cursor: String?,
        num: Int,
        callback: LoadCallback<Thread>
    ) {
        val threadsQuery = ThreadsSummaryQuery
            .builder()
            .after(cursor)
            .first(num)
            .build()

        patchqlApollo.query(threadsQuery) {
            it.map(::list)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql query failed. ${it}")
                }
        }
    }

    private fun list(it: Response<*>): List<Thread> {
        val data = it.data() as ThreadsSummaryQuery.Data

        return data.threads().edges().map { edge ->
            val root = edge.node().root()
            val cursor = edge.cursor()
            val repliesLength = edge.node().replies().size

            val rootPost = Post(
                root.id(),
                root.text(),
                root.likesCount(),
                root.author().name(),
                root.author().imageLink()
            )

            Thread(rootPost, repliesLength, cursor.orEmpty())
        }
    }
}