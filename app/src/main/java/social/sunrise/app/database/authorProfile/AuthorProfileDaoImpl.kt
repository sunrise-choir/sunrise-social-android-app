package social.sunrise.app.database.authorProfile

import androidx.lifecycle.MutableLiveData
import com.sunrisechoir.graphql.AuthorProfileQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import social.sunrise.app.PublishAboutMessage
import social.sunrise.app.PublishContactMessage
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.lib.AuthorRelationship
import social.sunrise.app.models.Author
import social.sunrise.app.models.LiveAuthor
import social.sunrise.app.models.PatchqlBackgroundMessage
import social.sunrise.app.models.ProcessNextChunk
import social.sunrise.app.dao.Author as AuthorDao

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

    override fun updateProfile(name: String, description: String, authorId: String) {
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val publishResponse = CompletableDeferred<Long>()
                ssbServer.await().send(
                    PublishAboutMessage(
                        authorId = authorId,
                        name = name,
                        description = description,
                        response = publishResponse
                    )
                )
                publishResponse.await()
                val processResponse = CompletableDeferred<Unit>()

                process.await().send(ProcessNextChunk(processResponse))
                processResponse.await()

                load()
            }
        }

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