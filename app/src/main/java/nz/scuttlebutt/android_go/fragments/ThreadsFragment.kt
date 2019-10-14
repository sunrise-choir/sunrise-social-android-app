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
import io.noties.markwon.Markwon
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.adapters.ThreadsAdapter
import nz.scuttlebutt.android_go.databinding.FragmentThreadsBinding
import nz.scuttlebutt.android_go.viewModels.ThreadsViewModel


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

        viewModel = ViewModelProviders.of(this).get(ThreadsViewModel::class.java)

        val layoutManager = LinearLayoutManager(context)
        binding.threads.layoutManager = layoutManager

        viewAdapter = ThreadsAdapter(viewModel::like, this, viewModel.markwon)

        binding.threads.adapter = viewAdapter

        viewModel.threadsLiveData.observe(this, Observer { list ->
            viewAdapter.submitList(list)
        })

        return binding.root
    }
}
