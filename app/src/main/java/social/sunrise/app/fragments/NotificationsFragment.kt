package social.sunrise.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import social.sunrise.app.R
import social.sunrise.app.adapters.PostsAdapter
import social.sunrise.app.databinding.FragmentNotificationsBinding
import social.sunrise.app.viewModels.NotificationsViewModel


class NotificationsFragment : Fragment() {


    private lateinit var viewModel: NotificationsViewModel
    private lateinit var viewAdapter: PostsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding = DataBindingUtil.inflate<FragmentNotificationsBinding>(
            inflater,
            R.layout.fragment_notifications,
            container,
            false
        )


        viewModel = ViewModelProviders.of(this).get(NotificationsViewModel::class.java)

        val layoutManager = LinearLayoutManager(context)
        binding.posts.layoutManager = layoutManager

        viewAdapter = PostsAdapter(viewModel::like, this, viewModel.markwon, viewModel::getBlob)

        binding.posts.adapter = viewAdapter


        viewModel.postsLiveData.observe(this, Observer { list ->
            viewAdapter.submitList(list)
        })


        return binding.root
    }

}
