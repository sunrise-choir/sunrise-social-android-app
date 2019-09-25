package nz.scuttlebutt.android_go.viewModels

import android.os.Environment
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.StartServer
import nz.scuttlebutt.android_go.StopServer
import nz.scuttlebutt.android_go.ssbServerActor

class MainActivityViewModel : ViewModel() {

    private lateinit var serverActor: SendChannel<SsbServerMsg>

    init {
        val externalDir = Environment.getExternalStorageDirectory().path
        val repoPath = externalDir + "/golog"

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                serverActor = this.ssbServerActor(repoPath)
                serverActor.send(StartServer)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                serverActor.send(StopServer)
            }
        }
    }
}