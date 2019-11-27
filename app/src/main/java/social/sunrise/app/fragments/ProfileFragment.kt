package social.sunrise.app.fragments


import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
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

        var authorId: String;
        try{
            val args = ProfileFragmentArgs.fromBundle(arguments!!)
            authorId = args.feedId
        }catch(e: Exception){
            authorId = viewModel.me
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

            when (it.relationshipToThem) {
                ContactState.FOLLOW -> {
                    binding.followButton.text = getText(R.string.unfollow_button)
                    binding.followButton.setOnClickListener { viewModel.unfollowAuthor(authorId) }
                }
                ContactState.NEUTRAL -> {
                    binding.followButton.text = getText(R.string.follow_button)
                    binding.followButton.setOnClickListener { viewModel.followAuthor(authorId) }
                }
            }


            if (it.imageLink != null) {
                viewModel.getBlob(it.imageLink).observe(this.viewLifecycleOwner, Observer {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    binding.avataarImageView.setImageBitmap(bitmap)
                })
            }

        })


        return binding.root
    }
}
