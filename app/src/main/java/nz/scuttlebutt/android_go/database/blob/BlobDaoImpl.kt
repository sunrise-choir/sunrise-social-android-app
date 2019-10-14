package nz.scuttlebutt.android_go.database.blob

import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import nz.scuttlebutt.android_go.GetBlob
import nz.scuttlebutt.android_go.SsbServerMsg
import nz.scuttlebutt.android_go.dao.Blob as BlobDao

class BlobDaoImpl(
    private val patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>
) : BlobDao {

    private val cache: LruCache<String, ByteArray> = LruCache(50)


    override fun create(blob: ByteArray): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(blobId: String): LiveData<ByteArray> {
        val liveBlob = MutableLiveData<ByteArray>()

        val blob = cache.get(blobId)
        if (blob != null) {
            liveBlob.postValue(blob)
        } else {
            val response: CompletableDeferred<ByteArray> = CompletableDeferred()

            GlobalScope.launch {
                withContext(Dispatchers.IO) {
                    ssbServer.await().send(GetBlob(blobId, response))
                    try {
                        val blob = response.await()
                        println("got blob!")
                        cache.put(blobId, blob)
                        liveBlob.postValue(blob)
                    } catch (e: Exception) {
                        println("failed to get blob")
                    }
                }
            }
        }

        return liveBlob
    }
}