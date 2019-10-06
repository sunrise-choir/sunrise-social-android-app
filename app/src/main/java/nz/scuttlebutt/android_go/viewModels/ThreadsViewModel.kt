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
import nz.scuttlebutt.android_go.models.Thread
import nz.scuttlebutt.android_go.models.ThreadsDataSourceFactory


class ThreadsViewModel(
    patchqlParams: Params,
    val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>
) : ViewModel() {
    val threadsLiveData: LiveData<PagedList<Thread>>
    private var patchql: PatchqlApollo = PatchqlApollo(patchqlParams)
    private val threadsDataSourceFactory = ThreadsDataSourceFactory(patchql)

    init{
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(10)
            .setPageSize(20).build()

        threadsLiveData = LivePagedListBuilder(threadsDataSourceFactory, pagedListConfig).build()
    }

    fun invalidateDataSource() = threadsDataSourceFactory.mutableLiveData.value?.invalidate()

}
