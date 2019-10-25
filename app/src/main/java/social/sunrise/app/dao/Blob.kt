package social.sunrise.app.dao

import androidx.lifecycle.LiveData

interface Blob {
    fun create(blob: ByteArray): String
    fun get(blobId: String): LiveData<ByteArray>
}