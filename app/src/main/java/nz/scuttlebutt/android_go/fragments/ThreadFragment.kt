package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunrisechoir.graphql.ThreadQuery
import com.sunrisechoir.graphql.ThreadQuery.Data
import com.sunrisechoir.patchql.PatchqlApollo
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.EndlessRecyclerViewScrollListener
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.ThreadAdapter
import nz.scuttlebutt.android_go.databinding.FragmentThreadBinding


/**
 * A simple [Fragment] subclass.
 */
class ThreadFragment : Fragment() {

    data class Post(val text: String, val likesCount: Int, val authorName: String?, val authorImageLink: String? )
    private lateinit var threadRootId: String

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private lateinit var viewAdapter: ThreadAdapter
    private lateinit var markWon: Markwon
    private lateinit var patchqlApollo: PatchqlApollo


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = ThreadFragmentArgs.fromBundle(arguments!!)
        threadRootId = args.threadRootId

        val binding: FragmentThreadBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_thread, container, false
            )
        markWon = Markwon.create(context!!)

        val externalDir = Environment.getExternalStorageDirectory().path
        val repoPath = externalDir + "/golog"
        val dbPath = context?.getDatabasePath("db.sqlite")?.absolutePath
        val offsetlogPath = repoPath + "/log"

        val pubKey = "@U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.ed25519"
        val privateKey = "123abc==.ed25519"
        patchqlApollo = PatchqlApollo()
        patchqlApollo.new(
            offsetLogPath = offsetlogPath,
            databasePath = dbPath!!,
            publicKey = pubKey,
            privateKey = privateKey
        )

        val recyclerView: RecyclerView = binding.root.findViewById(R.id.thread)
        val layoutManager = LinearLayoutManager(context)
        viewAdapter = ThreadAdapter(
            Array<Post>(0) { return null },
            markWon,
            this
        )

        recyclerView.layoutManager = layoutManager
        recyclerView.adapter = viewAdapter

        scrollListener = object : EndlessRecyclerViewScrollListener(layoutManager) {
            override fun onLoadMore(
                page: Int,
                cursor: String?,
                totalItemsCount: Int,
                view: RecyclerView
            ) {
                getNextThreads(patchqlApollo, recyclerView, totalItemsCount)
            }
        }

        getNextThreads(patchqlApollo, recyclerView, 0)


        return binding.root
    }

    private fun getNextThreads(
        apolloPatchql: PatchqlApollo,
        recyclerView: RecyclerView,
        totalItemsCount: Int
    ) {
        val threadsQuery = ThreadQuery.builder().rootId(threadRootId).build()
        apolloPatchql.query(threadsQuery) {
            val data: Data = it.getOrNull()?.data() as Data
            val newCursor = null

            recyclerView.post {
                scrollListener.currentCursor = newCursor

                val root = data.thread()?.root()!!
                val replies = data.thread()?.replies()!!
                val rootPost = Post(
                    root.text(),
                    root.likesCount(),
                    root.author().name(),
                    root.author().imageLink()
                )

                var nodes: Array<Post> = Array(1){rootPost}
                nodes = nodes.plus(replies.map {
                    Post(
                        it.text(),
                        it.likesCount(),
                        it.author().name(),
                        it.author().imageLink()
                    )
                })


                    //data.threads().edges().map { e -> e.node() }.toTypedArray()
                viewAdapter.myDataset = viewAdapter.myDataset.plus(nodes)
                recyclerView.adapter?.notifyItemRangeInserted(
                    totalItemsCount,
                    replies.size + 1
                )
            }

        }
    }






}
