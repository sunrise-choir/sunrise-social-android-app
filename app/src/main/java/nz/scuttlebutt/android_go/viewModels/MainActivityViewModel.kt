package nz.scuttlebutt.android_go.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.graphql.type.Privacy
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.StartServer
import nz.scuttlebutt.android_go.StopServer
import nz.scuttlebutt.android_go.database.Database
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

    private val database: Database by instance()

    private val me: String by instance("mySsbIdentity")

    var threadNotifications: Pair<LiveData<Int>, () -> Unit>
    var mentionsNotifications: Pair<LiveData<Int>, () -> Unit>
    var messagesNotifications: Pair<LiveData<Int>, () -> Unit>

    init {

        val threadNotificationQuery: () -> ThreadsSummaryQuery.Builder =
            { ThreadsSummaryQuery.builder() }

        val messagesNotificationQuery: () -> ThreadsSummaryQuery.Builder =
            { ThreadsSummaryQuery.builder().privacy(Privacy.PRIVATE) }

        val mentionsNotificationQuery: () -> PostsQuery.Builder =
            { PostsQuery.builder().mentionsAuthors(listOf(me)) }

        threadNotifications =
            database.notifcationsDao().getThreadsNotifications(threadNotificationQuery)
        messagesNotifications =
            database.notifcationsDao().getThreadsNotifications(messagesNotificationQuery)
        mentionsNotifications =
            database.notifcationsDao().getPostsNotifications(mentionsNotificationQuery)

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

    fun setThreadsNumberOfHops(numberOfHops: ThreadsNumberOfHops) {

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

    enum class ThreadsNumberOfHops {
        FOLLOWING,
        FOLLOWING_PLUS_ONE,
        ALL
    }
}
