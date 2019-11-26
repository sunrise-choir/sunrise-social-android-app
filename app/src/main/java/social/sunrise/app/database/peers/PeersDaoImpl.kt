package social.sunrise.app.database.peers

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import social.sunrise.app.GetPeers
import social.sunrise.app.Peer
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.dao.PeersDao


class PeersDaoImpl(val serverActor: CompletableDeferred<SendChannel<SsbServerMsg>>) : PeersDao {

    private val peers = MutableLiveData<List<Peer>>()

    override fun getAll(): LiveData<List<Peer>> {

        GlobalScope.launch {
            withContext(Dispatchers.Default) {
                while (true) {
                    val response = CompletableDeferred<List<Peer>>()
                    serverActor.await().send(GetPeers(response))


                    val newPeers = response.await()
                    val oldPeers = peers.value.orEmpty()

                    if (!(oldPeers.toTypedArray() contentEquals newPeers.toTypedArray())) {
                        peers.postValue(newPeers)
                    }

                    delay(5000)
                }
            }
        }

        return peers
    }
}