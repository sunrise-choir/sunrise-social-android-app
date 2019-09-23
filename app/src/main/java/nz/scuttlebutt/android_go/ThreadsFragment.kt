package nz.scuttlebutt.android_go


import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.sunrisechoir.patchql.PatchqlApollo
import com.sunrisechoir.graphql.ThreadsQuery
import com.sunrisechoir.graphql.ThreadsQuery.Data
import com.sunrisechoir.graphql.ThreadsQuery.Node
import io.noties.markwon.Markwon

import nz.scuttlebutt.android_go.databinding.FragmentThreadsBinding


/**
 * A simple [Fragment] subclass.
 */
class ThreadsFragment : Fragment() {

    private lateinit var viewModel: ThreadsViewModel

    private lateinit var scrollListener: EndlessRecyclerViewScrollListener
    private lateinit var viewAdapter: MyAdapter
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
            DataBindingUtil.inflate(inflater, R.layout.fragment_threads, container, false)

        val recyclerView: RecyclerView = binding.root.findViewById(R.id.threads)
        val layoutManager = LinearLayoutManager(context)
        viewAdapter = MyAdapter(Array<Node>(0) { return null }, markWon, this)

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
        val threadsQuery = ThreadsQuery.builder().before(cursor).last(20).build()
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

    class MyAdapter(var myDataset: Array<Node>, private var markWon: Markwon, val fragment: Fragment) :
        RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

        // Provide a reference to the views for each data item
        // Complex data items may need more than one view per item, and
        // you provide access to all the views for a data item in a view holder.
        // Each data item is just a string in this case that is shown in a TextView.
        class MyViewHolder(val cardView: View) : RecyclerView.ViewHolder(cardView)

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(
            parent: ViewGroup,
            viewType: Int
        ): MyViewHolder {
            // create a new view
            val cardView = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_thread_summary, parent, false)

            return MyViewHolder(cardView)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
            // - get element from your dataset at this position
            // - replace the contents of the view with that element
            val view = holder.cardView
            val data = myDataset[position]


            val rootPostTextView: TextView = view.findViewById(R.id.root_post_text)
            markWon.setMarkdown(rootPostTextView, data.root().text())


            val authorNameTextView: TextView = view.findViewById(R.id.author_name_text)
            authorNameTextView.text = data.root().author().name()

            val likesCountTextView: TextView = view.findViewById(R.id.likes_count_text)
            likesCountTextView.text = data.root().likesCount().toString()

            val repliesCountTextView: TextView = view.findViewById(R.id.replies_count_text)
            repliesCountTextView.text = data.replies().size.toString()


            val navController = Navigation.findNavController(fragment.requireActivity(), R.id.nav_host_fragment)

            view.setOnClickListener {
                if (navController.currentDestination?.id == R.id.threads_fragment){
                    navController.navigate(
                        ThreadsFragmentDirections.actionThreadsFragmentToThreadFragment(data.root().id())
                    )
                }
            }

        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = myDataset.size
    }


}
