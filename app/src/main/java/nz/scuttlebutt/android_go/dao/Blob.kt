package nz.scuttlebutt.android_go.dao

import androidx.lifecycle.LiveData

interface Blob {
    fun create(blob: ByteArray): String
    fun get(blobId: String): LiveData<ByteArray>
}