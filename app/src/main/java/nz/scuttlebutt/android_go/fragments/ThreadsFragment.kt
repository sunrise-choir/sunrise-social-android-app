package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.sunrisechoir.patchql.Params
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.ThreadsAdapter
import nz.scuttlebutt.android_go.databinding.FragmentThreadsBinding
import nz.scuttlebutt.android_go.viewModels.MainActivityViewModel
import nz.scuttlebutt.android_go.viewModels.ThreadsViewModel
import nz.scuttlebutt.android_go.viewModels.ThreadsViewModelFactory


/**
 * A simple [Fragment] subclass.
 */
class ThreadsFragment : Fragment() {

    private lateinit var viewModel: ThreadsViewModel
    private lateinit var viewAdapter: ThreadsAdapter
    private lateinit var markWon: Markwon

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        markWon = Markwon.create(context!!)
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

        binding.floatingActionButton.setOnClickListener {
            val navController = findNavController()
            navController.navigate(ThreadsFragmentDirections.actionThreadsFragmentToPublishFragment())
        }

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
            ThreadsViewModelFactory(
                Params(offsetlogPath, dbPath, pubKey, privateKey),
                activityModel!!.serverActor
            )
        viewModel = ViewModelProviders.of(this, factory).get(ThreadsViewModel::class.java)

        val threadViewModel: ThreadsViewModel =
            ViewModelProviders.of(this, factory).get(ThreadsViewModel::class.java)

        val layoutManager = LinearLayoutManager(context)
        binding.threads.layoutManager = layoutManager

        viewAdapter = ThreadsAdapter(viewModel.ssbServer)

        threadViewModel.threadsLiveData.observe(this, Observer { list ->
            viewAdapter.submitList(list)
        })
        binding.threads.adapter = viewAdapter

        return binding.root
    }
}
