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
import social.sunrise.app.R
import social.sunrise.app.databinding.FragmentEditProfileBinding
import social.sunrise.app.viewModels.EditProfileViewModel

/**
 * A simple [Fragment] subclass.
 */
class EditProfileFragment : Fragment() {
    private lateinit var viewModel: EditProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProviders.of(this).get(EditProfileViewModel::class.java)

        val binding: FragmentEditProfileBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_edit_profile, container, false
            )


        val author = viewModel.getAuthor(viewModel.me)

        author.observe(this, Observer {
            binding.editNameText.setText(it.name)
            binding.editDescriptionText.setText(it.description)

        })

        binding.saveButton.setOnClickListener {
            viewModel.updateProfile(
                name = binding.editNameText.text.toString(),
                description = binding.editDescriptionText.text.toString()
            )
            findNavController().navigateUp()
        }


        return binding.root
    }


}
