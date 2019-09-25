package nz.scuttlebutt.android_go

import gobotexample.Gobotexample
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration


@Serializable
data class Post(val text: String){
    val type = "post"
}

// Message types for counterActor
sealed class SsbServerMsg

object StartServer: SsbServerMsg()
object StopServer : SsbServerMsg()
class PublishMessage(val msgText: String, val response: CompletableDeferred<Long>) :
    SsbServerMsg() // a request with reply


// This function launches a new counter actor

fun CoroutineScope.ssbServerActor(repoPath: String) = actor<SsbServerMsg> {

    val json = Json(JsonConfiguration.Stable)
    val postSerializer = Post.serializer()
    var isServerRunning = false

    for (msg in channel) { // iterate over incoming messages
        when (msg) {
            is StartServer -> {
                if (!isServerRunning) {
                    println("starting sbot")
                    Gobotexample.start(repoPath)
                    isServerRunning = true
                    println("sbot started")
                }

            }
            is StopServer -> {
                if (isServerRunning) {
                    println("stopping sbot")
                    Gobotexample.stop()
                    isServerRunning = false
                    println("stopped sbot")
                    channel.close()

                }
            }
            is PublishMessage -> {
                println("got publish message")
                val recps = Gobotexample.newRecipientsCollection()
                val postMsg = Post(msg.msgText)
                val postJson = json.stringify(postSerializer, postMsg)

                println("Publishing message: \n$postJson")
                val seq: Long = Gobotexample.publish(
                    postJson,
                    recps.marshalJSON()
                )

                msg.response.complete(seq)
            }
        }
    }
}

