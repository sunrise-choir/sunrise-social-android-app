package social.sunrise.app.database

import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.dao.PeersDao
import social.sunrise.app.database.authorProfile.AuthorProfileDaoImpl
import social.sunrise.app.database.blob.BlobDaoImpl
import social.sunrise.app.database.notifications.NotificationsDaoImpl
import social.sunrise.app.database.peers.PeersDaoImpl
import social.sunrise.app.database.post.PostDaoImpl
import social.sunrise.app.database.threads.ThreadDaoImpl
import social.sunrise.app.database.threads.ThreadsDaoImpl
import social.sunrise.app.models.PatchqlBackgroundMessage
import social.sunrise.app.dao.Author as AuthorProfileDao
import social.sunrise.app.dao.Blob as BlobDao
import social.sunrise.app.dao.Notifications as NotificationsDao
import social.sunrise.app.dao.Post as PostDao
import social.sunrise.app.dao.Thread as ThreadDao
import social.sunrise.app.dao.Threads as ThreadsDao

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
    private val blobsDao = BlobDaoImpl(patchqlApollo, ssbServer)
    private val peersDao = PeersDaoImpl(ssbServer)

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

    fun blobsDao(): BlobDao {
        return blobsDao
    }

    fun peersDao(): PeersDao {
        return peersDao
    }

}

