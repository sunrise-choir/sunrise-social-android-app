package social.sunrise.app.database.threads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.apollographql.apollo.api.Response
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.patchql.PatchqlApollo
import social.sunrise.app.models.Post
import social.sunrise.app.models.Thread

class ThreadsDataSource(
    private val patchqlApollo: PatchqlApollo,
    private val queryBuilder: () -> ThreadsSummaryQuery.Builder,
    private val threads: MutableMap<String, MutableLiveData<Thread>>
) :
    ItemKeyedDataSource<String, LiveData<Thread>>() {


    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<LiveData<Thread>>
    ) {
        loadThreadsBefore(params.requestedInitialKey, params.requestedLoadSize, callback)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<LiveData<Thread>>) {
        loadThreadsAfter(params.key, params.requestedLoadSize, callback)

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<LiveData<Thread>>) {
        loadThreadsBefore(params.key, params.requestedLoadSize, callback)
    }

    override fun getKey(item: LiveData<Thread>): String {
        return item.value?.cursor.orEmpty()
    }

    private fun loadThreadsBefore(
        cursor: String?,
        num: Int,
        callback: LoadCallback<LiveData<Thread>>
    ) {

        val threadsQuery = queryBuilder()
            .before(cursor)
            .last(num)
            .build()

        patchqlApollo.query(threadsQuery) {
            it.map(::responseIntoThreads)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql queryBuilder failed. ${it}")
                }
        }
    }

    private fun loadThreadsAfter(
        cursor: String?,
        num: Int,
        callback: LoadCallback<LiveData<Thread>>
    ) {
        val query = queryBuilder()
            .after(cursor)
            .first(num)
            .build()

        patchqlApollo.query(query) {
            it.map(::responseIntoThreads)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql queryBuilder failed. ${it}")
                }
        }
    }


    private fun responseIntoThreads(it: Response<*>): List<LiveData<Thread>> {

        val data = it.data() as ThreadsSummaryQuery.Data

        return data.threads().edges().map { edge ->
            val root = edge.node().root()
            val cursor = edge.cursor()
            val repliesLength = edge.node().replies().size

            val rootPost = Post(
                id = root.id(),
                text = root.text(),
                likesCount = root.likesCount(),
                likedByMe = root.likedByMe(),
                authorId = root.author().id(),
                authorName = root.author().name(),
                authorImageLink = root.author().imageLink(),
                referencesLength = root.references().size,
                repliesCount = repliesLength,
                cursor = null,
                assertedTime = root.assertedTimestamp()?.toLong(),
                rootId = null
            )

            Thread(rootPost, cursor.orEmpty())
        }.map {
            val liveThread = threads[it.root.id]
            if (liveThread == null)
                threads[it.root.id] = MutableLiveData(it)
            else
                liveThread.postValue(it)

            threads[it.root.id]!!
        }

    }
}