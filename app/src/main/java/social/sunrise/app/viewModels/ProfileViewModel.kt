package social.sunrise.app.viewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sunrisechoir.graphql.AuthorProfileQuery
import io.noties.markwon.Markwon
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import social.sunrise.app.database.Database
import social.sunrise.app.lib.AuthorRelationship
import social.sunrise.app.models.LiveAuthor


class ProfileViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)

    private val database: Database by instance()
    val markwon: Markwon by instance()
    val me: String by instance("mySsbIdentity")

    fun getAuthor(authorId: String): LiveAuthor {
        val query = { AuthorProfileQuery.builder().id(authorId).me(me) }
        return database.authorProfileDao().get(query)
    }

    fun followAuthor(authorId: String) {
        return database.authorProfileDao().changeRelationship(AuthorRelationship.FOLLOW, authorId)
    }

    fun unfollowAuthor(authorId: String) {
        return database.authorProfileDao().changeRelationship(AuthorRelationship.UNFOLLOW, authorId)
    }

    fun getBlob(blobId: String) = database.blobsDao().get(blobId = blobId)

}
