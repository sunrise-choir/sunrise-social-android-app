package nz.scuttlebutt.android_go.database.authorProfile

import androidx.lifecycle.MutableLiveData
import com.sunrisechoir.graphql.AuthorProfileQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.PublishContactMessage
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.lib.AuthorRelationship
import nz.scuttlebutt.android_go.models.Author
import nz.scuttlebutt.android_go.models.LiveAuthor
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.models.ProcessNextChunk
import nz.scuttlebutt.android_go.dao.Author as AuthorDao

class AuthorProfileDaoImpl(
    private val patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    private val process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) : AuthorDao {

    private val author = MutableLiveData<Author>()
    private var query: (() -> AuthorProfileQuery.Builder)? = null

    override fun get(query: () -> AuthorProfileQuery.Builder): LiveAuthor {
        this.query = query

        load()

        return author
    }

    fun load() {
        patchqlApollo.query(query!!().build()) {
            it.map {
                it.data() as AuthorProfileQuery.Data
            }.map {
                Author.fromAuthorProfile(it)
            }.onSuccess {
                author.postValue(it)
            }.onFailure {
                throw Error("error getting author profile")
            }
        }
    }

    override fun changeRelationship(relationship: AuthorRelationship, authorId: String) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val publishResponse = CompletableDeferred<Long>()
                ssbServer.await().send(
                    PublishContactMessage(relationship, authorId, publishResponse)
                )
                publishResponse.await()
                val processResponse = CompletableDeferred<Unit>()

                process.await().send(ProcessNextChunk(processResponse))
                processResponse.await()

                load()
            }
        }
    }
}