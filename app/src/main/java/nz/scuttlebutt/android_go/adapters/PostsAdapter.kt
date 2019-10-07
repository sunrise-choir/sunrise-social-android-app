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
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.databinding.FragmentThreadSummaryBinding
import nz.scuttlebutt.android_go.models.LIVE_DIFF_CALLBACK
import nz.scuttlebutt.android_go.models.Post


class PostsAdapter(
    val likePost: (String, Boolean) -> Unit,
    val lifecycleOwner: LifecycleOwner
) :
    PagedListAdapter<LiveData<Post>, RecyclerView.ViewHolder>(LIVE_DIFF_CALLBACK) {

    private lateinit var markWon: Markwon

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder.
    // Each data item is just a string in this case that is shown in a TextView.
    inner class PostsViewHolder(
        private val binding: FragmentThreadSummaryBinding,
        private val markwon: Markwon,
        val navController: NavController
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bindTo(livePost: LiveData<Post>) {

            livePost.observe(lifecycleOwner, Observer { binding.fragmentPost.post = it })

            val post = livePost.value!!

            binding.fragmentPost.post = post

            markwon.setMarkdown(binding.fragmentPost.rootPostText, post.text)

            val likesIconImage = binding.fragmentPost.likesIconImage
            val image =
                if (post.likedByMe) R.drawable.ic_favorite_fuscia_24dp else R.drawable.ic_favorite_border_black_24dp
            likesIconImage.setImageResource(image)

//            if (post.authorImageLink != null) {
//                GlobalScope.launch {
//                    withContext(Dispatchers.IO) {
//                        val response = CompletableDeferred<ByteArray>()
//                        ssbServer.await().send(
//                            GetBlob(
//                                post.authorImageLink,
//                                response
//                            )
//                        )
//                        try {
//                            val bytes = response.await()
//                            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
//                            binding.fragmentPost.authorImage.post {
//                                binding.fragmentPost.authorImage.setImageBitmap(bmp)
//                            }
//
//
//                        } catch (_: Exception) {
//
//                        }
//
//
//                    }
//                }
//            }

            likesIconImage.setOnClickListener {
                likePost(post.id, !post.likedByMe)
            }
        }
    }

    // Create new views (invoked by the layout manager)
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): PostsViewHolder {
        markWon = Markwon.create(parent.context)

        val inflater = LayoutInflater.from(parent.context)
        val binding = FragmentThreadSummaryBinding.inflate(inflater)
        val navController = parent.findNavController()

        return PostsViewHolder(
            binding,
            markWon,
            navController
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as PostsViewHolder).bindTo(getItem(position)!!)
    }

}