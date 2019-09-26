package nz.scuttlebutt.android_go.models


import android.util.Log
import com.sunrisechoir.graphql.ProcessMutation
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.delay

sealed class PatchqlBackgroundMessage

object StopServer : PatchqlBackgroundMessage()


fun CoroutineScope.patchqlBackgroundActor(patchql: PatchqlApollo, delayTime: Long) =
    actor<PatchqlBackgroundMessage> {

        val tag = "PATCHQL_BACKGROUND"

        val processMutation = ProcessMutation.builder().chunkSize(10000).build()
        var latestSequence: Double? = 0.0

        outer_loop@ while (true) { // iterate over incoming messages

            val deferred = CompletableDeferred<Result<ProcessMutation.Data>>()
            patchql.query(processMutation) {
                deferred.complete(it.map { it.data() } as Result<ProcessMutation.Data>)
            }
            val processResult = deferred.await()
            processResult.onSuccess {
                val newSequence = it.process().latestSequence()
                if (newSequence == latestSequence) {
                    delay(delayTime)
                }
                latestSequence = newSequence
                Log.v(tag, "Processed a chunk")
            }

            when (channel.poll()) {
                is StopServer -> {
                    break@outer_loop
                }
            }
        }
    }
