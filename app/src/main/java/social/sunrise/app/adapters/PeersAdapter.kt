package social.sunrise.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import social.sunrise.app.Peer
import social.sunrise.app.databinding.FragmentPeerBinding


class PeersAdapter(var peers: List<Peer>) : BaseAdapter() {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val inflater = LayoutInflater.from(parent?.context)
        val binding = FragmentPeerBinding.inflate(inflater)

        val peer = getItem(position)

        binding.peer = peer

        return binding.root
    }

    override fun getItem(position: Int): Peer {
        return peers[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getCount(): Int {
        return peers.size
    }
}