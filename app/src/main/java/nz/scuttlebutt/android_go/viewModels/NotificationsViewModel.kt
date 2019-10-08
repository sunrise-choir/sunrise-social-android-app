package nz.scuttlebutt.android_go.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.graphql.PostsQuery
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.database.Database
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance

class NotificationsViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)
    private val me: String by instance("mySsbIdentity")

    private val database: Database by instance()
    val markwon: Markwon by instance()

    val query = { PostsQuery.builder().mentionsAuthors(listOf(me)) }

    val postsDataSourceFactory = database.postDao().getAllPaged(query)

    val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setInitialLoadSizeHint(10)
        .setPageSize(20).build()

    val postsLiveData = LivePagedListBuilder(postsDataSourceFactory, pagedListConfig).build()

    fun like(postId: String, doesLike: Boolean) {
        database.postDao().like(postId, doesLike)
    }
}