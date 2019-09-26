package nz.scuttlebutt.android_go.viewModels

import android.os.Environment
import androidx.lifecycle.ViewModel
import com.sunrisechoir.patchql.Params
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.StartServer
import nz.scuttlebutt.android_go.StopServer
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.models.patchqlBackgroundActor
import nz.scuttlebutt.android_go.ssbServerActor

class MainActivityViewModel(patchqlParams: Params) : ViewModel() {

    var serverActor: CompletableDeferred<SendChannel<SsbServerMsg>> = CompletableDeferred()
    var patchqlBackgroundActor: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>> =
        CompletableDeferred()
    private var patchql: PatchqlApollo = PatchqlApollo(patchqlParams)

    init {
        val externalDir = Environment.getExternalStorageDirectory().path
        val repoPath = externalDir + "/golog"

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val actor = ssbServerActor(repoPath)
                serverActor.complete(actor)
                serverActor.await().send(StartServer)
            }
        }

        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                val patchqlActor = patchqlBackgroundActor(patchql, 2000)
                patchqlBackgroundActor.complete(patchqlActor)

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
        GlobalScope.launch {
            withContext(Dispatchers.IO) {
                patchqlBackgroundActor.await().send(nz.scuttlebutt.android_go.models.StopServer)
            }
        }
    }
}