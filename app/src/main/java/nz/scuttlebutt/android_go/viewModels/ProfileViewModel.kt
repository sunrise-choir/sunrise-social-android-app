package nz.scuttlebutt.android_go.viewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sunrisechoir.graphql.AuthorProfileQuery
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.database.Database
import nz.scuttlebutt.android_go.lib.AuthorRelationship
import nz.scuttlebutt.android_go.models.LiveAuthor
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class ProfileViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)

    private val database: Database by instance()
    val markwon: Markwon by instance()
    private val me: String by instance("mySsbIdentity")

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

}
