package nz.scuttlebutt.android_go.database

import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.database.post.PostDaoImpl
import nz.scuttlebutt.android_go.database.post.ThreadsDaoImpl
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.dao.Post as PostDao
import nz.scuttlebutt.android_go.dao.Threads as ThreadsDao

class Database(
    patchqlApollo: PatchqlApollo,
    ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>,
    process: CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>
) {
    private val postDao = PostDaoImpl(patchqlApollo, ssbServer, process)
    private val threadsDao = ThreadsDaoImpl(patchqlApollo, ssbServer, process)

    fun postDao(): PostDao {
        return postDao
    }

    fun threadsDao(): ThreadsDao {
        return threadsDao
    }
}

