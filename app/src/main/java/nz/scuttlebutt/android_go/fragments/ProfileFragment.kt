package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.databinding.FragmentProfileBinding


class ProfileFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val binding: FragmentProfileBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_profile, container, false
            )

        return binding.root
    }
}
