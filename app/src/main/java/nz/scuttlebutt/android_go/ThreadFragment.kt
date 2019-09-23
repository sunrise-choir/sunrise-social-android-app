package nz.scuttlebutt.android_go


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import nz.scuttlebutt.android_go.databinding.FragmentThreadBinding
import nz.scuttlebutt.android_go.databinding.FragmentThreadsBinding

/**
 * A simple [Fragment] subclass.
 */
class ThreadFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args = ThreadFragmentArgs.fromBundle(arguments!!)
        println("args: ${args.threadRootId}")

        val binding: FragmentThreadBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_thread, container, false)

        return binding.root
    }






}
