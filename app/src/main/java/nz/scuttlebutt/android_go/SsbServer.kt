package nz.scuttlebutt.android_go

import android.util.Log
import gobotexample.Gobotexample
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


@Serializable
data class Post(val text: String){
    val type = "post"
}


@Serializable
data class Like(@Transient val link: String = "", @Transient val value: Int = 0) {
    val type = "vote"
    val vote = Vote(link, value)

    @Serializable
    data class Vote(val link: String, val value: Int)
}

// Message types for counterActor
sealed class SsbServerMsg

object StartServer: SsbServerMsg()
object StopServer : SsbServerMsg()

class PublishLikeMessage(
    val msgId: String,
    val doesLike: Boolean,
    val response: CompletableDeferred<Long>
) :
    SsbServerMsg() // a request with reply

class PublishPostMessage(val msgText: String, val response: CompletableDeferred<Long>) :
    SsbServerMsg() // a request with reply

class GetBlob(val ref: String, val response: CompletableDeferred<ByteArray>) : SsbServerMsg()
// This function launches a new counter actor

fun CoroutineScope.ssbServerActor(repoPath: String) = actor<SsbServerMsg> {

    val tag = "SSB_SERVER"
    val json = Json(JsonConfiguration.Stable)
    val postSerializer = Post.serializer()
    val likeSerializer = Like.serializer()
    var isServerRunning = false

    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is StartServer -> {
                if (!isServerRunning) {
                    Log.i(tag, "starting sbot")
                    Gobotexample.start(repoPath)
                    isServerRunning = true
                    Log.i(tag, "starting sbot")
                }

            }
            is StopServer -> {
                if (isServerRunning) {
                    Log.i(tag, "stopping sbot")
                    Gobotexample.stop()
                    isServerRunning = false
                    Log.i(tag, "stopped sbot")
                    channel.close()

                }
            }
            is GetBlob -> {
                Gobotexample.blobsWant(msg.ref)
                try {
                    val blob = Gobotexample.blobsGet(msg.ref)
                    msg.response.complete(blob)
                } catch (e: Exception) {
                    msg.response.completeExceptionally(e)
                }
            }
            is PublishLikeMessage -> {
                val recps = Gobotexample.newRecipientsCollection()
                Log.i(tag, "Published like for ${msg.msgId}, doesLike ${msg.doesLike}")
                val likeMessage = Like(msg.msgId, if (msg.doesLike) 1 else 0)
                val likeJson = json.stringify(likeSerializer, likeMessage)
                val seq: Long = Gobotexample.publish(
                    likeJson,
                    recps.marshalJSON()
                )
                msg.response.complete(seq)
            }
            is PublishPostMessage -> {

                val recps = Gobotexample.newRecipientsCollection()
                val postMsg = Post(msg.msgText)
                val postJson = json.stringify(postSerializer, postMsg)

                val seq: Long = Gobotexample.publish(
                    postJson,
                    recps.marshalJSON()
                )

                msg.response.complete(seq)
            }
        }
    }
}

