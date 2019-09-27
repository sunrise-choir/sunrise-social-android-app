package nz.scuttlebutt.android_go.models

import androidx.paging.ItemKeyedDataSource
import com.apollographql.apollo.api.Response
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.patchql.PatchqlApollo

class PostsDataSource(private val patchqlApollo: PatchqlApollo, val query: String) :
    ItemKeyedDataSource<String, Post>() {


    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<Post>
    ) {
        loadPostsBefore(params.requestedInitialKey, params.requestedLoadSize, callback)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<Post>) {
        loadPostsAfter(params.key, params.requestedLoadSize, callback)

    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<Post>) {
        loadPostsBefore(params.key, params.requestedLoadSize, callback)
    }

    override fun getKey(item: Post): String {
        return item.cursor.orEmpty()
    }

    private fun loadPostsBefore(
        cursor: String?,
        num: Int,
        callback: LoadCallback<Post>
    ) {
        val PostsQuery = PostsQuery
            .builder()
            .query(query)
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
        callback: LoadCallback<Post>
    ) {
        val PostsQuery = PostsQuery
            .builder()
            .query(query)
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

    private fun responseIntoPosts(it: Response<*>): List<Post> {
        val data = it.data() as PostsQuery.Data

        return data.posts().edges().map { edge ->
            val root = edge.node()
            val cursor = edge.cursor()

            Post(
                root.id(),
                root.text(),
                root.likesCount().toString(),
                root.likedByMe(),
                root.author().name(),
                root.author().imageLink(),
                root.references().size.toString(),
                null,
                cursor
            )
        }
    }
}