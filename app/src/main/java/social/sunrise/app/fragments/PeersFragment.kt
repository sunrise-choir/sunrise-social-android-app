package social.sunrise.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import social.sunrise.app.Peer
import social.sunrise.app.R
import social.sunrise.app.adapters.PeersAdapter
import social.sunrise.app.databinding.FragmentPeersBinding
import social.sunrise.app.viewModels.PeersViewModel

class PeersFragment : Fragment() {

    private lateinit var viewModel: PeersViewModel
    private lateinit var viewAdapter: PeersAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val binding: FragmentPeersBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_peers, container, false
            )

        viewModel = ViewModelProviders.of(this).get(PeersViewModel::class.java)
        viewAdapter = PeersAdapter(viewModel.peers.value.orEmpty())

        viewModel.peers.observe(this, Observer {
            println("$it")
            viewAdapter.peers = it
            viewAdapter.notifyDataSetChanged()
        })

        binding.peers.adapter = viewAdapter

        binding.peers.onItemClickListener =
            AdapterView.OnItemClickListener { parent, view, position, _ ->
                val peer: Peer = parent.getItemAtPosition(position) as Peer
                findNavController().navigate(
                    PeersFragmentDirections.actionPeersFragmentToProfileHolderFragment(
                        peer.Id
                    )
                )
            }

        return binding.root
    }
}
