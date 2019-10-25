package social.sunrise.app.database.threads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.sunrisechoir.graphql.ThreadForPostQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import social.sunrise.app.PublishLikeMessage
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.models.PatchqlBackgroundMessage
import social.sunrise.app.models.Post
import social.sunrise.app.models.ProcessNextChunk
import social.sunrise.app.dao.Thread as ThreadDao

class ThreadDaoImpl(
    private val patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    private val process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) : ThreadDao {

    private val posts: MutableMap<String, MutableLiveData<Post>> = mutableMapOf()
    private lateinit var postsDataSource: ThreadDataSource

    override fun reload(rootId: String) {

        val postsQuery = ThreadForPostQuery.builder().postId(rootId).build()

        patchqlApollo.query(postsQuery) {
            it.map(postsDataSource::responseIntoPosts)
                .getOrNull()
        }
    }

    override fun getAllPaged(query: () -> ThreadForPostQuery.Builder): DataSource.Factory<String, LiveData<Post>> {

        return object : DataSource.Factory<String, LiveData<Post>>() {

            var mutableLiveData: MutableLiveData<ThreadDataSource> = MutableLiveData()

            override fun create(): DataSource<String, LiveData<Post>> {

                postsDataSource = ThreadDataSource(
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