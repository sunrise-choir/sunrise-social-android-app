package social.sunrise.app.database.post

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.sunrisechoir.graphql.PostQuery
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import social.sunrise.app.PublishLikeMessage
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.models.PatchqlBackgroundMessage
import social.sunrise.app.models.Post
import social.sunrise.app.models.ProcessNextChunk
import social.sunrise.app.dao.Post as PostDao

class PostDaoImpl(
    private val patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    private val process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) : PostDao {

    private val posts: MutableMap<String, MutableLiveData<Post>> = mutableMapOf()

    override fun reload(postId: String) {
        val existingPost = posts[postId]!!

        val PostQuery = PostQuery
            .builder()
            .id(postId)
            .build()

        patchqlApollo.query(PostQuery) {
            it.map {
                it.data() as PostQuery.Data
            }
                .onSuccess {
                    val oldPost = existingPost.value!!
                    val newPost = it.post()!!
                    existingPost.postValue(
                        oldPost.copy(
                            likesCount = newPost.likesCount(),
                            likedByMe = newPost.likedByMe()
                        )
                    )

                }
                .onFailure {
                    throw Error("patchql query failed. ${it}")
                }
        }

    }

    override fun getAllPaged(query: () -> PostsQuery.Builder): DataSource.Factory<String, LiveData<Post>> {

        return object : DataSource.Factory<String, LiveData<Post>>() {

            var mutableLiveData: MutableLiveData<PostsDataSource> = MutableLiveData()
            private lateinit var postsDataSource: PostsDataSource


            override fun create(): DataSource<String, LiveData<Post>> {

                postsDataSource = PostsDataSource(
                    patchqlApollo,
                    query,
                    posts
                )
                mutableLiveData.postValue(postsDataSource)
                return postsDataSource
            }
        }
    }

    override fun like(postId: String, doesLike: Boolean) {

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val publishResponse = CompletableDeferred<Long>()
                ssbServer.await().send(
                    PublishLikeMessage(
                        postId,
                        doesLike,
                        publishResponse
                    )
                )
                publishResponse.await()
                val processResponse = CompletableDeferred<Unit>()

                process.await().send(ProcessNextChunk(processResponse))
                processResponse.await()

                reload(postId)
            }
        }
    }
}