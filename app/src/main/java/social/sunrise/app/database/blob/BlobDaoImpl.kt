package social.sunrise.app.database.blob

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.collection.LruCache
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import social.sunrise.app.GetBlob
import social.sunrise.app.SsbServerMsg
import social.sunrise.app.dao.Blob as BlobDao

class BlobDaoImpl(
    private val patchqlApollo: PatchqlApollo,
    private val ssbServer: CompletableDeferred<SendChannel<SsbServerMsg>>
) : BlobDao {

    private val cache: LruCache<String, Bitmap> = LruCache(50)


    override fun create(blob: ByteArray): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun get(blobId: String): LiveData<Bitmap> {
        val liveBlob = MutableLiveData<Bitmap>()

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
                        val bitmap = BitmapFactory.decodeByteArray(blob, 0, blob.size)
                        cache.put(blobId, bitmap)
                        liveBlob.postValue(bitmap)
                    } catch (e: Exception) {
                        println("failed to get blob")
                    }
                }
            }
        }

        return liveBlob
    }
}