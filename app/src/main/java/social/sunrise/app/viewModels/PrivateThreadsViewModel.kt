package social.sunrise.app.viewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.graphql.type.OrderBy
import com.sunrisechoir.graphql.type.Privacy
import io.noties.markwon.Markwon
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import social.sunrise.app.database.Database


class PrivateThreadsViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)

    private val database: Database by instance()
    val markwon: Markwon by instance()

    val query = { ThreadsSummaryQuery.builder().privacy(Privacy.PRIVATE).orderBy(OrderBy.ASSERTED) }

    val threadsDataSourceFactory = database.threadsDao().getAllPaged(query)

    val pagedListConfig = PagedList.Config.Builder()
        .setEnablePlaceholders(false)
        .setInitialLoadSizeHint(1)
        .setPageSize(1).build()

    val threadsLiveData = LivePagedListBuilder(threadsDataSourceFactory, pagedListConfig).build()

    fun like(postId: String, doesLike: Boolean) {
        //TODO this needs to publish a private like message
        database.threadsDao().like(postId, doesLike)
    }

    fun getBlob(blobId: String) = database.blobsDao().get(blobId = blobId)

}
