package nz.scuttlebutt.android_go.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.graphql.PostsQuery
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.database.Database
import nz.scuttlebutt.android_go.models.Post
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class SearchViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)

    private val database: Database by instance()
    val markwon: Markwon by instance()

    var postsLiveData: LiveData<PagedList<LiveData<Post>>>? = null
    private lateinit var postsDataSourceFactory: DataSource.Factory<String, LiveData<Post>>

    fun search(queryString: String){
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(10)
            .setPageSize(20).build()


        val searchQuery = { PostsQuery.builder().query(queryString) }
        postsDataSourceFactory = database.postDao().getAllPaged(searchQuery)
        postsLiveData = LivePagedListBuilder(postsDataSourceFactory, pagedListConfig).build()
    }

    fun like(postId: String, doesLike: Boolean) {
        database.postDao().like(postId, doesLike)
    }

}