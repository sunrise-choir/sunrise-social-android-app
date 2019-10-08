package nz.scuttlebutt.android_go.fragments


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.PostsAdapter
import nz.scuttlebutt.android_go.databinding.FragmentSearchBinding
import nz.scuttlebutt.android_go.viewModels.SearchViewModel


/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    private lateinit var viewModel: SearchViewModel
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

        viewModel = ViewModelProviders.of(this).get(SearchViewModel::class.java)

        val layoutManager = LinearLayoutManager(context)
        binding.posts.layoutManager = layoutManager

        viewAdapter = PostsAdapter(viewModel::like, this, viewModel.markwon)

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

            val view = this.view!!.rootView
            hideKeyboardFrom(this.context!!, view)

        }

        return binding.root
    }
}

fun hideKeyboardFrom(context: Context, view: View) {
    val imm = context.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(view.windowToken, 0)
    view.clearFocus()
}
