package nz.scuttlebutt.android_go.viewModels

import android.os.Environment
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.StartServer
import nz.scuttlebutt.android_go.StopServer
import nz.scuttlebutt.android_go.ssbServerActor

class MainActivityViewModel : ViewModel() {

    var serverActor: CompletableDeferred<SendChannel<SsbServerMsg>> = CompletableDeferred()


    init {
        val externalDir = Environment.getExternalStorageDirectory().path
        val repoPath = externalDir + "/golog"

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val actor = this.ssbServerActor(repoPath)
                serverActor.complete(actor)
                serverActor.await().send(StartServer)
            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                serverActor.await().send(StopServer)
            }
        }
    }
}