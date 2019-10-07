package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.PostsAdapter
import nz.scuttlebutt.android_go.databinding.FragmentSearchBinding
import nz.scuttlebutt.android_go.viewModels.PostsViewModel

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    private lateinit var viewModel: PostsViewModel
    private lateinit var viewAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentSearchBinding>(
            inflater,
            R.layout.fragment_search,
            container,
            false
        )

        viewModel = ViewModelProviders.of(this).get(PostsViewModel::class.java)

        val layoutManager = LinearLayoutManager(context)
        binding.posts.layoutManager = layoutManager

        viewAdapter = PostsAdapter(viewModel::like, this)

        binding.posts.adapter = viewAdapter

        if (viewModel.postsLiveData != null) {
            viewModel.postsLiveData?.observe(this, Observer { list ->
                viewAdapter.submitList(list)
            })
        }

        binding.searchButton.setOnClickListener {
            val queryString = binding.searchEditText.text.toString()
            viewModel.search(queryString)
            viewModel.postsLiveData?.observe(this, Observer { list ->
                viewAdapter.submitList(list)
            })
        }

        return binding.root
    }


}
