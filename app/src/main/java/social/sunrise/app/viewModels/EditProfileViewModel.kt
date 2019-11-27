package social.sunrise.app.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sunrisechoir.graphql.AuthorProfileQuery
import io.noties.markwon.Markwon
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import social.sunrise.app.database.Database
import social.sunrise.app.models.LiveAuthor

class EditProfileViewModel(
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

    fun updateProfile(name: String, description: String) {
        database.authorProfileDao()
            .updateProfile(name = name, description = description, authorId = me)
    }

}