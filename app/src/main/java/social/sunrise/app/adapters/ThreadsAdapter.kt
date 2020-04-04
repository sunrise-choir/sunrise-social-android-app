package social.sunrise.app.adapters


import android.graphics.Bitmap
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
import social.sunrise.app.NavigationDirections
import social.sunrise.app.R
import social.sunrise.app.databinding.FragmentThreadSummaryBinding
import social.sunrise.app.fragments.ThreadFragmentDirections
import social.sunrise.app.models.LIVE_THREAD_DIFF_CALLBACK
import social.sunrise.app.models.Thread
import java.util.*


class ThreadsAdapter(
    val likePost: (String, Boolean) -> Unit,
    val lifecycleOwner: LifecycleOwner,
    val markwon: Markwon,
    val getBlob: (String) -> LiveData<Bitmap>

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
            val authorName = binding.fragmentPost.authorNameText

            liveThread.observe(lifecycleOwner, Observer {
                binding.fragmentPost.post = it.root

                val image =
                    if (it.root.likedByMe) R.drawable.ic_favorite_fuscia_24dp else R.drawable.ic_favorite_border_black_24dp
                likesIconImage.setImageResource(image)
            })

            binding.fragmentPost.authorImage.setImageResource(R.drawable.ic_person_black_24dp)
            if (thread.root.authorImageLink != null) {
                getBlob(thread.root.authorImageLink).observe(lifecycleOwner, Observer {

                    binding.fragmentPost.authorImage.setImageBitmap(it)
                })
            }

            val assertedTime = thread.root.assertedTime
            if (assertedTime != null) {
                binding.fragmentPost.postTimeTextView.setReferenceTime(Date(assertedTime).time)
            }

            markwon.setMarkdown(binding.fragmentPost.rootPostText, thread.root.text)
            binding.fragmentPost.post = thread.root

            likesIconImage.setOnClickListener {
                val post = liveThread.value!!.root
                likePost(post.id, !post.likedByMe)
            }
            authorImage.setOnClickListener {
                val post = liveThread.value!!
                navigateToAuthor(post.root.authorId)
            }

            authorName.setOnClickListener{
                val postValue = liveThread.value!!
                navigateToAuthor(postValue.root.authorId)
            }

            //It's odd that we need to set a click listener on the text as well as the root, but so be it. It works.
            binding.fragmentPost.root.setOnClickListener { navigateToThread(thread) }
            binding.fragmentPost.rootPostText.setOnClickListener { navigateToThread(thread) }

        }

        private fun navigateToThread(thread: Thread) {
            navController.navigate(
                ThreadFragmentDirections.actionGlobalThreadFragment(
                    thread.root.id,
                    null
                )
            )
        }

        private fun navigateToAuthor(authorId: String) {

            navController.navigate(
                NavigationDirections.actionGlobalProfileHolderFragment(authorId)
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
