package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.paging.PagedList
import androidx.recyclerview.widget.LinearLayoutManager
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.PostsAdapter
import nz.scuttlebutt.android_go.databinding.FragmentThreadBinding
import nz.scuttlebutt.android_go.models.Post
import nz.scuttlebutt.android_go.viewModels.ThreadViewModel


/**
 * A simple [Fragment] subclass.
 */
class ThreadFragment : Fragment() {


    private lateinit var postId: String

    private lateinit var viewModel: ThreadViewModel
    private lateinit var viewAdapter: PostsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = ThreadFragmentArgs.fromBundle(arguments!!)

        // We can either pass the id of some post that is a member of the thread, or the root id of the thread.
        // If the root is defined, use that, otherwise use the id of the member post. (Which we will scroll to)
        postId = args.threadRootId ?: args.postId!!

        val binding: FragmentThreadBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_thread, container, false
            )

        //TODO reply button
//        binding.floatingActionButton.setOnClickListener {
//            val navController = findNavController()
//            navController.navigate(ThreadsFragmentDirections.actionThreadsFragmentToPublishFragment())
//        }

        viewModel = ViewModelProviders.of(this).get(ThreadViewModel::class.java)
        viewModel.setPostId(postId)

        val layoutManager = LinearLayoutManager(context)
        binding.thread.layoutManager = layoutManager


        viewAdapter = PostsAdapter(viewModel::like, this, viewModel.markwon)

        binding.thread.adapter = viewAdapter

        viewModel.threadLiveData.observe(this, Observer { list: PagedList<LiveData<Post>> ->
            viewAdapter.submitList(list)
        })

        return binding.root
    }




}
