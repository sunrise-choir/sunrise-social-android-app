package social.sunrise.app.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.graphql.PostsQuery
import io.noties.markwon.Markwon
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import social.sunrise.app.database.Database
import social.sunrise.app.models.Post

class PostsViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)
    val me: String by instance("mySsbIdentity")

    private val database: Database by instance()
    val markwon: Markwon by instance()

    lateinit var query: () -> PostsQuery.Builder
    private lateinit var postsDataSourceFactory: DataSource.Factory<String, LiveData<Post>>

    private lateinit var pagedListConfig: PagedList.Config

    lateinit var postsLiveData: LiveData<PagedList<LiveData<Post>>>

    init {
        // The default author is `me` but it can be configured by calling `setNotificationsAuthor`
        setNotificationsAuthor(me)
    }

    fun like(postId: String, doesLike: Boolean) {
        database.postDao().like(postId, doesLike)
    }

    fun getBlob(blobId: String) = database.blobsDao().get(blobId = blobId)

    fun setNotificationsAuthor(author: String) {
        query = { PostsQuery.builder().authors(listOf(author)) }

        postsDataSourceFactory = database.postDao().getAllPaged(query)

        pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(1)
            .setPageSize(3).build()

        postsLiveData = LivePagedListBuilder(postsDataSourceFactory, pagedListConfig).build()
    }
}