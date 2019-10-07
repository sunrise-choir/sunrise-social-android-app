package nz.scuttlebutt.android_go.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.StartServer
import nz.scuttlebutt.android_go.StopServer
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.models.patchqlBackgroundActor
import nz.scuttlebutt.android_go.ssbServerActor
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance


class MainActivityViewModel(app: Application) : AndroidViewModel(app), KodeinAware {

    override val kodein by kodein(app)

    val serverActor: CompletableDeferred<SendChannel<SsbServerMsg>> by instance("ssbServerActor")
    val patchqlBackgroundActor: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>> by instance(
        "patchqlProcessActor"
    )

    private val patchql: PatchqlApollo by instance()
    private val repoPath: String by instance("repoPath")

    init {

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