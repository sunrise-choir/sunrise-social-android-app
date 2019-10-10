package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.databinding.FragmentProfileBinding
import nz.scuttlebutt.android_go.viewModels.ProfileViewModel


class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = ProfileFragmentArgs.fromBundle(arguments!!)
        val authorId = args.feedId //TODO change feedId to be author id

        // Inflate the layout for this fragment
        val binding: FragmentProfileBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_profile, container, false
            )

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val author = viewModel.getAuthor(authorId)

        author.observe(this, Observer {
            binding.author = it
            viewModel.markwon.setMarkdown(binding.descriptionTextView, it.description.orEmpty())
        })


        return binding.root
    }
}
