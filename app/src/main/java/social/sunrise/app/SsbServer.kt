package social.sunrise.app

import android.util.Log
import gobotexample.Gobotexample
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.list
import social.sunrise.app.lib.AuthorRelationship


@Serializable
data class Peer(val IP: String, val Id: String)

@Serializable
data class Post(val text: String) {
    val type = "post"
}

@Serializable
data class Contact(val contact: String, val following: Boolean, val blocking: Boolean) {
    val type = "contact"
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

object StartServer : SsbServerMsg()
object StopServer : SsbServerMsg()

class PublishLikeMessage(
    val msgId: String,
    val doesLike: Boolean,
    val response: CompletableDeferred<Long>
) :
    SsbServerMsg() // a request with reply

class PublishPostMessage(val msgText: String, val response: CompletableDeferred<Long>) :
    SsbServerMsg() // a request with reply

class PublishContactMessage(
    val relationship: AuthorRelationship,
    val authorId: String,
    val response: CompletableDeferred<Long>
) :
    SsbServerMsg()

class GetPeers(val response: CompletableDeferred<List<Peer>>) : SsbServerMsg()
class GetBlob(val ref: String, val response: CompletableDeferred<ByteArray>) : SsbServerMsg()
// This function launches a new counter actor

fun CoroutineScope.ssbServerActor(repoPath: String) = actor<SsbServerMsg> {

    val tag = "SSB_SERVER"
    val json = Json(JsonConfiguration.Stable)
    val postSerializer = Post.serializer()
    val likeSerializer = Like.serializer()
    val contactSerializer = Contact.serializer()
    var isServerRunning = false

    outer_loop@ for (msg in channel) { // iterate over incoming messages
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
                    //channel.close()
                    //break@outer_loop

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
            is GetPeers -> {
                try {
                    val peersString: ByteArray = Gobotexample.peers()
                    val peers: List<Peer> =
                        Json.nonstrict.parse(Peer.serializer().list, String(peersString))
                    msg.response.complete(peers)
                } catch (e: Exception) {
                    msg.response.completeExceptionally(e)
                }
            }
            is PublishLikeMessage -> {
                val recps = Gobotexample.newRecipientsCollection()
                Log.i(tag, "Published like for ${msg.msgId}, doesLike ${msg.doesLike}")
                val likeMessage = Like(msg.msgId, if (msg.doesLike) 1 else 0)
                val likeJson = json.stringify(likeSerializer, likeMessage)
                Gobotexample.publish(
                    likeJson,
                    recps.marshalJSON()
                )
                msg.response.complete(0)
            }
            is PublishPostMessage -> {

                val recps = Gobotexample.newRecipientsCollection()
                val postMsg = Post(msg.msgText)
                val postJson = json.stringify(postSerializer, postMsg)

                Gobotexample.publish(
                    postJson,
                    recps.marshalJSON()
                )

                msg.response.complete(0)
            }
            is PublishContactMessage -> {

                val recps = Gobotexample.newRecipientsCollection()
                val relationship = msg.relationship
                val authorId = msg.authorId
                val contactMsg: Contact = when (relationship) {
                    AuthorRelationship.BLOCK -> Contact(
                        contact = authorId,
                        blocking = true,
                        following = false
                    )
                    AuthorRelationship.FOLLOW -> Contact(
                        contact = authorId,
                        blocking = false,
                        following = true
                    )
                    AuthorRelationship.UNBLOCK -> Contact(
                        contact = authorId,
                        blocking = false,
                        following = false
                    )
                    AuthorRelationship.UNFOLLOW -> Contact(
                        contact = authorId,
                        blocking = false,
                        following = false
                    )
                }
                val contactJson = json.stringify(contactSerializer, contactMsg)
                Gobotexample.publish(contactJson, recps.marshalJSON())


                msg.response.complete(0)

                Gobotexample.stop()
                Gobotexample.start(repoPath)
            }
        }
    }
}

