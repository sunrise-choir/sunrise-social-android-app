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
import com.sunrisechoir.patchql.Params
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.PostsAdapter
import nz.scuttlebutt.android_go.databinding.FragmentSearchBinding
import nz.scuttlebutt.android_go.viewModels.MainActivityViewModel
import nz.scuttlebutt.android_go.viewModels.PostsViewModel
import nz.scuttlebutt.android_go.viewModels.PostsViewModelFactory

/**
 * A simple [Fragment] subclass.
 */
class SearchFragment : Fragment() {

    private lateinit var viewModel: PostsViewModel
    private lateinit var viewAdapter: PostsAdapter
    private lateinit var markWon: Markwon

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

        val externalDir = "/sdcard"
        val repoPath = externalDir + getString(R.string.ssb_go_folder_name)
        val dbPath =
            context?.getDatabasePath(getString(R.string.patchql_sqlite_db_name))?.absolutePath!!
        val offsetlogPath = repoPath + "/log"

        val pubKey = "@U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.ed25519"
        val privateKey = "123abc==.ed25519"

        val activityModel = activity?.run {
            ViewModelProviders.of(this)[MainActivityViewModel::class.java]
        }
        val factory =
            PostsViewModelFactory(
                Params(offsetlogPath, dbPath, pubKey, privateKey),
                activityModel!!.serverActor
            )
        viewModel = ViewModelProviders.of(this, factory).get(PostsViewModel::class.java)

        val layoutManager = LinearLayoutManager(context)
        binding.posts.layoutManager = layoutManager

        viewAdapter = PostsAdapter(viewModel.ssbServer)


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
