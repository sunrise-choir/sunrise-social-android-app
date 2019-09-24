package nz.scuttlebutt.android_go.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.fragments.ThreadsFragmentDirections

class ThreadsAdapter(
    var myDataset: Array<ThreadsSummaryQuery.Node>,
    private var markWon: Markwon,
    val fragment: Fragment
) :
    RecyclerView.Adapter<ThreadsAdapter.ThreadsViewHolder>() {

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    class ThreadsViewHolder(val cardView: View) : RecyclerView.ViewHolder(cardView)

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ThreadsViewHolder {
        // create a new view
        val cardView = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_thread_summary, parent, false)

        return ThreadsViewHolder(
            cardView
        )
    }

    // Replace the contents of a view (invoked by the layout manager)
    override fun onBindViewHolder(holder: ThreadsViewHolder, position: Int) {
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


        val navController = Navigation.findNavController(
            fragment.requireActivity(),
            R.id.nav_host_fragment
        )

        view.setOnClickListener {
            if (navController.currentDestination?.id == R.id.threads_fragment) {
                navController.navigate(
                    ThreadsFragmentDirections.actionThreadsFragmentToThreadFragment(
                        data.root().id()
                    )
                )
            }
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    override fun getItemCount() = myDataset.size
}