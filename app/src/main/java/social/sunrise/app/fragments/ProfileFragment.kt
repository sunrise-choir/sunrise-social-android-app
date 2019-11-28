package social.sunrise.app.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.sunrisechoir.graphql.type.ContactState
import social.sunrise.app.R
import social.sunrise.app.databinding.FragmentProfileBinding
import social.sunrise.app.viewModels.ProfileViewModel


class ProfileFragment : Fragment() {
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        viewModel = ViewModelProviders.of(this).get(ProfileViewModel::class.java)

        val authorId = try {
            val args = ProfileFragmentArgs.fromBundle(arguments!!)
            args.feedId!!
        }catch(e: Exception){
            viewModel.me
        }

        // Inflate the layout for this fragment
        val binding: FragmentProfileBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_profile, container, false
            )

        val author = viewModel.getAuthor(authorId)

        author.observe(this, Observer {
            binding.author = it
            viewModel.markwon.setMarkdown(binding.descriptionTextView, it.description.orEmpty())

            when (Pair(authorId == viewModel.me, it.relationshipToThem)) {
                Pair(false, ContactState.FOLLOW) -> {
                    binding.followButton.text = getText(R.string.unfollow_button)
                    binding.followButton.setOnClickListener { viewModel.unfollowAuthor(authorId) }
                }
                Pair(false, ContactState.NEUTRAL) -> {
                    binding.followButton.text = getText(R.string.follow_button)
                    binding.followButton.setOnClickListener { viewModel.followAuthor(authorId) }
                }
                else -> {
                    binding.followButton.text = "Edit"
                    binding.followButton.setOnClickListener {
                        println("author id is $authorId")
                        findNavController().navigate(
                            ProfileHolderFragmentDirections.actionProfileHolderFragmentToEditProfileFragment(
                                authorId
                            )
                        )
                    }
                }
            }


            if (it.imageLink != null) {
                viewModel.getBlob(it.imageLink).observe(this.viewLifecycleOwner, Observer {

                    binding.avataarImageView.setImageBitmap(it)
                })
            }

        })


        return binding.root
    }
}
