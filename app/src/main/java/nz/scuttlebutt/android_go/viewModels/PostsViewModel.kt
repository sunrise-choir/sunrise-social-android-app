package nz.scuttlebutt.android_go.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.DataSource
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.patchql.Params
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.database.Database
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.models.Post


class PostsViewModel(
    patchqlParams: Params,
    val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    patchqlBackgroundActor: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) : ViewModel() {
    var postsLiveData: LiveData<PagedList<LiveData<Post>>>? = null
    private var patchql: PatchqlApollo = PatchqlApollo(patchqlParams)
    private lateinit var postsDataSourceFactory: DataSource.Factory<String, LiveData<Post>>
    val database: Database = Database(patchql, ssbServer, patchqlBackgroundActor)

    fun search(queryString: String){
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(10)
            .setPageSize(20).build()



        postsDataSourceFactory = database.postDao().getAllPaged(queryString)
        postsLiveData = LivePagedListBuilder(postsDataSourceFactory, pagedListConfig).build()
        //postsDataSourceFactory = PostsDataSourceFactory(patchql, queryString)
        //postsLiveData = LivePagedListBuilder(postsDataSourceFactory, pagedListConfig).build()

    }

    fun updatePost(post: Post) {
        database.postDao().save(post)
    }

    //fun invalidateDataSource() = postsDataSourceFactory.mutableLiveData.value?.invalidate()

}