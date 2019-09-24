package nz.scuttlebutt.android_go.models

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.sunrisechoir.patchql.PatchqlApollo


class ThreadsDataSourceFactory(private val patchqlApollo: PatchqlApollo) :
    DataSource.Factory<String, Thread>() {

    var mutableLiveData: MutableLiveData<ThreadsDataSource> = MutableLiveData()
    private lateinit var threadsDataSource: ThreadsDataSource

    override fun create(): DataSource<String, Thread> {
        threadsDataSource = ThreadsDataSource(patchqlApollo)
        mutableLiveData.postValue(threadsDataSource)
        return threadsDataSource
    }

}