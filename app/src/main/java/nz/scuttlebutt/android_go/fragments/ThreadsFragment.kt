package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.graphql.ThreadsSummaryQuery.Data
import com.sunrisechoir.graphql.ThreadsSummaryQuery.Node
import com.sunrisechoir.patchql.PatchqlApollo
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.EndlessRecyclerViewScrollListener
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.ThreadsAdapter
import nz.scuttlebutt.android_go.databinding.FragmentThreadsBinding
import nz.scuttlebutt.android_go.viewModels.ThreadsViewModel


/**
 * A simple [Fragment] subclass.
 */
class ThreadsFragment : Fragment() {

    private lateinit var viewModel: ThreadsViewModel

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private lateinit var viewAdapter: ThreadsAdapter
    private lateinit var markWon: Markwon
    private lateinit var patchqlApollo: PatchqlApollo

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(ThreadsViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val binding: FragmentThreadsBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_threads, container, false
            )

        val recyclerView: RecyclerView = binding.root.findViewById(R.id.threads)
        val layoutManager = LinearLayoutManager(context)
        viewAdapter = ThreadsAdapter(
            Array<Node>(0) { return null },
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
                getNextThreads(cursor, patchqlApollo, recyclerView, totalItemsCount)
            }
        }

        getNextThreads(null, patchqlApollo, recyclerView, 0)

        recyclerView.addOnScrollListener(scrollListener)


        return binding.root
    }

    private fun getNextThreads(
        cursor: String?,
        apolloPatchql: PatchqlApollo,
        recyclerView: RecyclerView,
        totalItemsCount: Int
    ) {
        val threadsQuery = ThreadsSummaryQuery.builder().before(cursor).last(20).build()
        apolloPatchql.query(threadsQuery) {
            val data: Data = it.getOrNull()?.data() as Data
            val newCursor = data.threads().pageInfo().endCursor()

            recyclerView.post {
                scrollListener.currentCursor = newCursor
                val nodes: Array<Node> = data.threads().edges().map { e -> e.node() }.toTypedArray()
                viewAdapter.myDataset = viewAdapter.myDataset.plus(nodes)
                recyclerView.adapter?.notifyItemRangeInserted(
                    totalItemsCount,
                    data.threads().edges().size
                )
            }

        }
    }




}
