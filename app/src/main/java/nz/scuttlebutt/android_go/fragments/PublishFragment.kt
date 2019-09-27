package nz.scuttlebutt.android_go.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.databinding.FragmentPublishBinding

/**
 * A simple [Fragment] subclass.
 */
class PublishFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        //return inflater.inflate(R.layout.fragment_publish, container, false)
        val binding = DataBindingUtil.inflate<FragmentPublishBinding>(
            inflater,
            R.layout.fragment_publish,
            container,
            false
        )


        val adapter = ArrayAdapter<String>(
            this.context!!,
            android.R.layout.simple_dropdown_item_1line, COUNTRIES
        )
        binding.autoCompleteTextView.setAdapter(adapter)

        return binding.root
    }

    private val COUNTRIES = arrayOf("Belgium", "France", "Italy", "Germany", "Spain")


}
