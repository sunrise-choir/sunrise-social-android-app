package social.sunrise.app.models


import android.util.Log
import com.sunrisechoir.graphql.ProcessMutation
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withTimeoutOrNull

sealed class PatchqlBackgroundMessage

object StopServer : PatchqlBackgroundMessage()
class ProcessNextChunk(val response: CompletableDeferred<Unit>) : PatchqlBackgroundMessage()


// This needs a refactor!

// This "actor" is just a loop. It's job is to call the `process` mutation to keep the patchql db up to date with the sbot log.
// - When doing an initial sync, it calls the process mutation immediately after processing the last chunk.
// - In "normal" use the loop delays for for `delayTime` ms if the last process mutation did not process any new data.
// - If the UI has done something that means patchql is out of date,
//   it sends a ProcessNextChunk message that will process immediately rather than waiting for a timeout.

fun CoroutineScope.patchqlBackgroundActor(patchql: PatchqlApollo, delayTime: Long) =
    actor<PatchqlBackgroundMessage> {

        val tag = "PATCHQL_BACKGROUND"

        val processMutation = ProcessMutation.builder().chunkSize(10000).build()
        var latestSequence: Double? = 0.0

        outer_loop@ while (true) { // iterate over incoming messages
            var msg: PatchqlBackgroundMessage? = null


            process(patchql, processMutation).await().onSuccess {
                val newSequence = it.process().latestSequence()
                if (newSequence == latestSequence) {
                    withTimeoutOrNull(delayTime) {
                        msg = channel.receive()
                    }
                }
                latestSequence = newSequence
                Log.v(tag, "Processed a chunk")
            }

            when (msg) {
                is StopServer -> {
                    break@outer_loop
                }
                is ProcessNextChunk -> {
                    process(patchql, processMutation).await().onSuccess {
                        latestSequence = it.process().latestSequence()

                    }
                    (msg as ProcessNextChunk).response.complete(Unit)
                }
            }
        }
    }

private suspend fun process(
    patchql: PatchqlApollo,
    processMutation: ProcessMutation
): CompletableDeferred<Result<ProcessMutation.Data>> {
    val deferred = CompletableDeferred<Result<ProcessMutation.Data>>()
    patchql.query(processMutation) {
        deferred.complete(it.map { it.data() } as Result<ProcessMutation.Data>)
    }
    return deferred
}
