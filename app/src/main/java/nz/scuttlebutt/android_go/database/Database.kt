package nz.scuttlebutt.android_go.database

import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.database.authorProfile.AuthorProfileDaoImpl
import nz.scuttlebutt.android_go.database.notifications.NotificationsDaoImpl
import nz.scuttlebutt.android_go.database.post.PostDaoImpl
import nz.scuttlebutt.android_go.database.threads.ThreadDaoImpl
import nz.scuttlebutt.android_go.database.threads.ThreadsDaoImpl
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.dao.Author as AuthorProfileDao
import nz.scuttlebutt.android_go.dao.Notifications as NotificationsDao
import nz.scuttlebutt.android_go.dao.Post as PostDao
import nz.scuttlebutt.android_go.dao.Thread as ThreadDao
import nz.scuttlebutt.android_go.dao.Threads as ThreadsDao

class Database(
    patchqlApollo: PatchqlApollo,
    ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) {
    private val postDao = PostDaoImpl(patchqlApollo, ssbServer, process)
    private val threadsDao =
        ThreadsDaoImpl(patchqlApollo, ssbServer, process)

    private val threadDao = ThreadDaoImpl(patchqlApollo, ssbServer, process)
    private val authorProfileDao = AuthorProfileDaoImpl(patchqlApollo, ssbServer, process)
    private val notificationsDao = NotificationsDaoImpl(patchqlApollo)

    fun postDao(): PostDao {
        return postDao
    }

    fun threadsDao(): ThreadsDao {
        return threadsDao
    }

    fun threadDao(): ThreadDao {
        return threadDao
    }

    fun authorProfileDao(): AuthorProfileDao {
        return authorProfileDao
    }

    fun notifcationsDao(): NotificationsDao {
        return notificationsDao
    }

}

