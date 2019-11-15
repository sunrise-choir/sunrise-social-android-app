package social.sunrise.app.dao

import androidx.lifecycle.LiveData
import social.sunrise.app.Peer

interface PeersDao {
    fun getAll(): LiveData<List<Peer>>
}