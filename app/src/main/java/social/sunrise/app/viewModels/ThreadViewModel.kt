package social.sunrise.app.viewModels


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.graphql.ThreadForPostQuery
import io.noties.markwon.Markwon
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import social.sunrise.app.database.Database
import social.sunrise.app.models.Post


class ThreadViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)

    private val database: Database by instance()
    val markwon: Markwon by instance()

    lateinit var threadLiveData: LiveData<PagedList<LiveData<Post>>>

    fun setPostId(postId: String) {
        val query = { ThreadForPostQuery.builder().postId(postId) }

        val threadDataSourceFactory = database.threadDao().getAllPaged(query)

        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(10)
            .setPageSize(20).build()

        threadLiveData = LivePagedListBuilder(threadDataSourceFactory, pagedListConfig).build()
    }

    fun like(postId: String, doesLike: Boolean) {
        database.threadDao().like(postId, doesLike)
    }

    fun getBlob(blobId: String) = database.blobsDao().get(blobId = blobId)
}
