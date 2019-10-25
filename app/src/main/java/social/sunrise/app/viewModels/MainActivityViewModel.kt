package social.sunrise.app.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.graphql.type.Privacy
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.StartServer
import social.sunrise.app.StopServer
import social.sunrise.app.database.Database
import social.sunrise.app.models.PatchqlBackgroundMessage
import social.sunrise.app.models.patchqlBackgroundActor
import social.sunrise.app.ssbServerActor


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
            withContext(Dispatchers.Default) {
                val actor = ssbServerActor(repoPath)
                serverActor.complete(actor)
                serverActor.await().send(StartServer)
            }
        }

        GlobalScope.launch {
            withContext(Dispatchers.Default) {
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
                patchqlBackgroundActor.await().send(social.sunrise.app.models.StopServer)
            }
        }
    }

    enum class ThreadsNumberOfHops {
        FOLLOWING,
        FOLLOWING_PLUS_ONE,
        ALL
    }
}
