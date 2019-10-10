package nz.scuttlebutt.android_go.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.NavigationDirections
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.databinding.FragmentThreadSummaryBinding
import nz.scuttlebutt.android_go.fragments.ThreadsFragmentDirections
import nz.scuttlebutt.android_go.models.LIVE_THREAD_DIFF_CALLBACK
import nz.scuttlebutt.android_go.models.Thread


class ThreadsAdapter(
    val likePost: (String, Boolean) -> Unit,
    val lifecycleOwner: LifecycleOwner,
    val markwon: Markwon
) :
    PagedListAdapter<LiveData<Thread>, RecyclerView.ViewHolder>(LIVE_THREAD_DIFF_CALLBACK) {

    inner class ThreadsViewHolder(
        private val binding: FragmentThreadSummaryBinding,
        val navController: NavController
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindTo(liveThread: LiveData<Thread>) {

            val thread = liveThread.value!!

            val likesIconImage = binding.fragmentPost.likesIconImage
            val authorImage = binding.fragmentPost.authorImage

            liveThread.observe(lifecycleOwner, Observer {
                binding.fragmentPost.post = it.root

                val image =
                    if (it.root.likedByMe) R.drawable.ic_favorite_fuscia_24dp else R.drawable.ic_favorite_border_black_24dp
                likesIconImage.setImageResource(image)
            })

            binding.fragmentPost.post = thread.root

            markwon.setMarkdown(binding.fragmentPost.rootPostText, thread.root.text)

            likesIconImage.setOnClickListener {
                val post = liveThread.value!!.root
                likePost(post.id, !post.likedByMe)
            }
            authorImage.setOnClickListener {
                val post = liveThread.value!!
                navigateToAuthor(post.root.authorId)

            }

            //It's odd that we need to set a click listener on the text as well as the root, but so be it. It works.
            binding.fragmentPost.root.setOnClickListener { navigateToThread(thread) }
            binding.fragmentPost.rootPostText.setOnClickListener { navigateToThread(thread) }

        }

        private fun navigateToThread(thread: Thread) {
            if (navController.currentDestination?.id != R.id.threads_fragment)
                return
            navController.navigate(
                ThreadsFragmentDirections.actionThreadsFragmentToThreadFragment(
                    thread.root.id,
                    null
                )
            )
        }

        private fun navigateToAuthor(authorId: String) {
            if (navController.currentDestination?.id != R.id.threads_fragment)
                return
            navController.navigate(
                NavigationDirections.actionGlobalProfileFragment(authorId)
            )
        }

    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ThreadsViewHolder {


        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentThreadSummaryBinding.inflate(inflater)
        val navController = parent.findNavController()

        return ThreadsViewHolder(
            binding,
            navController
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ThreadsViewHolder).bindTo(getItem(position)!!)
    }

}
