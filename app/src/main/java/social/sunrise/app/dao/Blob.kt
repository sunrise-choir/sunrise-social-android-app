package social.sunrise.app.dao

import android.graphics.Bitmap
import androidx.lifecycle.LiveData

interface Blob {
    fun create(blob: ByteArray): String
    fun get(blobId: String): LiveData<Bitmap>
}