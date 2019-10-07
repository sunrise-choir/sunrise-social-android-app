package nz.scuttlebutt.android_go.database

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import androidx.paging.ItemKeyedDataSource
import com.apollographql.apollo.api.Response
import com.sunrisechoir.graphql.PostQuery
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.PublishLikeMessage
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.models.Post
import nz.scuttlebutt.android_go.models.ProcessNextChunk
import nz.scuttlebutt.android_go.dao.Post as PostDao

class Database(
    patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    private val process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) {

    private val posts: MutableMap<String, MutableLiveData<Post>> = mutableMapOf()

    private val postDao = object : PostDao {

        override fun reload(postCursor: String) {
            val existingPost = posts[postCursor]

            val PostQuery = PostQuery
                .builder()
                .id(existingPost!!.value!!.id)
                .build()

            patchqlApollo.query(PostQuery) {
                it.map {
                    it.data() as PostQuery.Data
                }
                    .onSuccess {
                        val oldPost = existingPost.value!!
                        existingPost.postValue(
                            oldPost.copy(
                                likesCount = it.post()!!.likesCount()
                            )
                        )

                    }
                    .onFailure {
                        throw Error("patchql query failed. ${it}")
                    }
            }

        }

        override fun getAllPaged(query: String): DataSource.Factory<String, LiveData<Post>> {

            return object : DataSource.Factory<String, LiveData<Post>>() {

                var mutableLiveData: MutableLiveData<PostsDataSource> = MutableLiveData()
                private lateinit var postsDataSource: PostsDataSource

                override fun create(): DataSource<String, LiveData<Post>> {

                    postsDataSource = PostsDataSource(patchqlApollo, query, posts)
                    mutableLiveData.postValue(postsDataSource)
                    return postsDataSource
                }
            }
        }

        override fun save(post: Post) {

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    val publishResponse = CompletableDeferred<Long>()
                    ssbServer.await().send(
                        PublishLikeMessage(
                            post.id,
                            !post.likedByMe,
                            publishResponse
                        )
                    )
                    publishResponse.await()
                    val processResponse = CompletableDeferred<Unit>()

                    process.await().send(ProcessNextChunk(processResponse))
                    processResponse.await()

                    reload(post.cursor!!)
                }
            }

        }
    }

    fun postDao(): PostDao {
        return postDao
    }


}

class PostsDataSource(
    private val patchqlApollo: PatchqlApollo,
    private val query: String,
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
        callback: LoadCallback<LiveData<Post>>
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
            val livePost = posts[it.cursor!!]
            if (livePost == null)
                posts[it.cursor] = MutableLiveData(it)
            else
                livePost.postValue(it)

            posts[it.cursor]!!
        }

    }
}

