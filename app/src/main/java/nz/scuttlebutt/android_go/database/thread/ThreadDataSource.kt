package nz.scuttlebutt.android_go.database.threads

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.ItemKeyedDataSource
import com.apollographql.apollo.api.Response
import com.sunrisechoir.graphql.ThreadForPostQuery
import com.sunrisechoir.graphql.fragment.PostFragment
import com.sunrisechoir.patchql.PatchqlApollo
import nz.scuttlebutt.android_go.models.Post


class ThreadDataSource(
    private val patchqlApollo: PatchqlApollo,
    private val queryBuilder: () -> ThreadForPostQuery.Builder,
    private val posts: MutableMap<String, MutableLiveData<Post>>
) :
    ItemKeyedDataSource<String, LiveData<Post>>() {

    override fun loadInitial(
        params: LoadInitialParams<String>,
        callback: LoadInitialCallback<LiveData<Post>>
    ) {
        loadPostsBefore(callback)
    }

    override fun loadBefore(params: LoadParams<String>, callback: LoadCallback<LiveData<Post>>) {
        //loadPostsAfter(callback)
    }

    override fun loadAfter(params: LoadParams<String>, callback: LoadCallback<LiveData<Post>>) {
        //loadPostsBefore(callback)
    }

    override fun getKey(item: LiveData<Post>): String {
        return item.value?.cursor.orEmpty()
    }

    private fun loadPostsBefore(
        callback: LoadCallback<LiveData<Post>>
    ) {

        val postsQuery = queryBuilder()
            .build()

        patchqlApollo.query(postsQuery) {
            it.map(::responseIntoPosts)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql queryBuilder failed. ${it}")
                }
        }
    }

    private fun loadPostsAfter(
        callback: LoadCallback<LiveData<Post>>
    ) {
        val query = queryBuilder()
            .build()

        patchqlApollo.query(query) {
            it.map(::responseIntoPosts)
                .onSuccess(callback::onResult)
                .onFailure {
                    throw Error("patchql queryBuilder failed. ${it}")
                }
        }
    }


    fun responseIntoPosts(it: Response<*>): List<LiveData<Post>> {

        val data = it.data() as ThreadForPostQuery.Data
        val rootFragment = data.threadForPost()?.root()?.fragments()?.postFragment()!!

        val root = postFragmentToPost(rootFragment)
        val replies = data.threadForPost()?.replies()!!
            .map { it.fragments().postFragment() }
            .map(this::postFragmentToPost)

        return listOf(root).plus(replies)
            .map {
                val livePost = posts[it.id]
                if (livePost == null)
                    posts[it.id] = MutableLiveData(it)
                else
                    livePost.postValue(it)

                posts[it.id]!!
            }


    }

    private fun postFragmentToPost(fragment: PostFragment): Post {
        return Post(
            id = fragment.id(),
            text = fragment.text(),
            likesCount = fragment.likesCount(),
            likedByMe = fragment.likedByMe(),
            authorId = fragment.author().id(),
            authorName = fragment.author().name(),
            authorImageLink = fragment.author().imageLink(),
            referencesLength = fragment.references().size,
            repliesCount = null,
            cursor = null,
            assertedTime = fragment.assertedTimestamp()?.toLong()
        )
    }
}