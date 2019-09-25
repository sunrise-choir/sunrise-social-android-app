package nz.scuttlebutt.android_go.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import com.sunrisechoir.patchql.Params
import com.sunrisechoir.patchql.PatchqlApollo
import nz.scuttlebutt.android_go.models.Thread
import nz.scuttlebutt.android_go.models.ThreadsDataSourceFactory


class ThreadsViewModel(patchqlParams: Params) : ViewModel() {
    val threadsLiveData: LiveData<PagedList<Thread>>
    private var patchql: PatchqlApollo = PatchqlApollo(patchqlParams)

    init{

        val threadsDataSourceFactory = ThreadsDataSourceFactory(patchql)
        val pagedListConfig = PagedList.Config.Builder()
            .setEnablePlaceholders(false)
            .setInitialLoadSizeHint(10)
            .setPageSize(20).build()

        threadsLiveData = LivePagedListBuilder(threadsDataSourceFactory, pagedListConfig).build()
    }

}