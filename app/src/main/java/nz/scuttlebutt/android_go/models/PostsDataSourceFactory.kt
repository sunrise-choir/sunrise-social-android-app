package nz.scuttlebutt.android_go.models

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource
import com.sunrisechoir.patchql.PatchqlApollo


class PostsDataSourceFactory(private val patchqlApollo: PatchqlApollo, private val query: String) :
    DataSource.Factory<String, Post>() {

    var mutableLiveData: MutableLiveData<PostsDataSource> = MutableLiveData()
    private lateinit var postsDataSource: PostsDataSource

    override fun create(): DataSource<String, Post> {
        postsDataSource = PostsDataSource(patchqlApollo, query)
        mutableLiveData.postValue(postsDataSource)
        return postsDataSource
    }

}