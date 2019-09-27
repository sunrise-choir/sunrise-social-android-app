package nz.scuttlebutt.android_go.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.patchql.Params
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.models.Post
import nz.scuttlebutt.android_go.models.PostsDataSourceFactory
import nz.scuttlebutt.android_go.models.ThreadsDataSourceFactory


class PostsViewModel(
    patchqlParams: Params,
    val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>
) : ViewModel() {
    lateinit var postsLiveData: LiveData<PagedList<Post>>
    private var patchql: PatchqlApollo = PatchqlApollo(patchqlParams)
    private lateinit var postsDataSourceFactory: PostsDataSourceFactory


    fun search(queryString: String){
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(10)
            .setPageSize(20).build()

        postsDataSourceFactory = PostsDataSourceFactory(patchql, queryString)
        postsLiveData = LivePagedListBuilder(postsDataSourceFactory, pagedListConfig).build()

    }

    fun invalidateDataSource() = postsDataSourceFactory.mutableLiveData.value?.invalidate()

}