package social.sunrise.app.database.threads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.sunrisechoir.graphql.PostQuery
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import social.sunrise.app.PublishLikeMessage
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.models.PatchqlBackgroundMessage
import social.sunrise.app.models.ProcessNextChunk
import social.sunrise.app.models.Thread
import social.sunrise.app.dao.Threads as ThreadsDao

class ThreadsDaoImpl(
    private val patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    private val process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) : ThreadsDao {

    private val threads: MutableMap<String, MutableLiveData<Thread>> = mutableMapOf()

    override fun reload(rootId: String) {
        val existingPost = threads[rootId]!!

        val PostQuery = PostQuery
            .builder()
            .id(rootId)
            .build()

        patchqlApollo.query(PostQuery) {
            it.map {
                it.data() as PostQuery.Data
            }
                .onSuccess {
                    val oldPost = existingPost.value!!
                    val newPost = it.post()!!

                    val oldRoot = existingPost.value!!.root
                    val newRoot = oldRoot.copy(
                        likesCount = newPost.likesCount(),
                        likedByMe = newPost.likedByMe()
                    )

                    existingPost.postValue(
                        oldPost.copy(
                            root = newRoot
                        )
                    )

                }
                .onFailure {
                    throw Error("patchql query failed. ${it}")
                }
        }

    }

    override fun getAllPaged(query: () -> ThreadsSummaryQuery.Builder): DataSource.Factory<String, LiveData<Thread>> {

        return object : DataSource.Factory<String, LiveData<Thread>>() {

            var mutableLiveData: MutableLiveData<ThreadsDataSource> = MutableLiveData()
            private lateinit var postsDataSource: ThreadsDataSource


            override fun create(): DataSource<String, LiveData<Thread>> {

                postsDataSource = ThreadsDataSource(
                    patchqlApollo,
                    query,
                    threads
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