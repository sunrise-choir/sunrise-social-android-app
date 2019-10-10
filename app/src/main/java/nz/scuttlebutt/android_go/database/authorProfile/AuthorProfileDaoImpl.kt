package nz.scuttlebutt.android_go.database.authorProfile

import androidx.lifecycle.MutableLiveData
import com.sunrisechoir.graphql.AuthorProfileQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.dao.AuthorRelationship
import nz.scuttlebutt.android_go.models.Author
import nz.scuttlebutt.android_go.models.LiveAuthor
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.dao.Author as AuthorDao

class AuthorProfileDaoImpl(
    private val patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    private val process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) : AuthorDao {

    private val author = MutableLiveData<Author>()

    override fun get(query: () -> AuthorProfileQuery.Builder): LiveAuthor {
        patchqlApollo.query(query().build()) {
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
        return author

    }

    override fun changeRelationship(relationship: AuthorRelationship) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}