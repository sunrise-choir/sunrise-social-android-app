package nz.scuttlebutt.android_go.database.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.apollographql.apollo.api.Response
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.patchql.PatchqlApollo
import nz.scuttlebutt.android_go.models.Post

class PostsDataSource(
    private val patchqlApollo: PatchqlApollo,
    private val query: () -> PostsQuery.Builder,
    private val posts: MutableMap<String, MutableLiveData<Post>>
) :
    ItemKeyedDataSource<String, LiveData<Post>>() {


    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<LiveData<Post>>
    ) {
        loadPostsBefore(params.requestedInitialKey, params.requestedLoadSize, callback)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<LiveData<Post>>) {
        loadPostsAfter(params.key, params.requestedLoadSize, callback)

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<LiveData<Post>>) {
        loadPostsBefore(params.key, params.requestedLoadSize, callback)
    }

    override fun getKey(item: LiveData<Post>): String {
        return item.value?.cursor.orEmpty()
    }

    private fun loadPostsBefore(
        cursor: String?,
        num: Int,
        callback: LoadCallback<LiveData<Post>>
    ) {

        val PostsQuery = query()
            .before(cursor)
            .last(num)
            .build()

        patchqlApollo.query(PostsQuery) {
            it.map(::responseIntoPosts)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql query failed. ${it}")
                }
        }
    }

    private fun loadPostsAfter(
        cursor: String?,
        num: Int,
        callback: LoadCallback<LiveData<Post>>
    ) {
        val PostsQuery = query()
            .after(cursor)
            .first(num)
            .build()

        patchqlApollo.query(PostsQuery) {
            it.map(::responseIntoPosts)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql query failed. ${it}")
                }
        }
    }

    private fun responseIntoPosts(it: Response<*>): List<LiveData<Post>> {
        val data = it.data() as PostsQuery.Data

        return data.posts().edges().map { edge ->
            val root = edge.node()
            val cursor = edge.cursor()

            Post(
                root.id(),
                root.text(),
                root.likesCount(),
                root.likedByMe(),
                root.author().name(),
                root.author().imageLink(),
                root.references().size,
                null,
                cursor
            )
        }.map {
            val livePost = posts[it.id]
            if (livePost == null)
                posts[it.id] = MutableLiveData(it)
            else
                livePost.postValue(it)

            posts[it.id]!!
        }

    }
}