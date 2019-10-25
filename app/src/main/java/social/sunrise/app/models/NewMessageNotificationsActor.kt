package social.sunrise.app.models


import androidx.lifecycle.MutableLiveData
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.withTimeoutOrNull

sealed class NewMessageNotificationsMessage

object Stop : NewMessageNotificationsMessage()
class Subscribe : NewMessageNotificationsMessage()

fun CoroutineScope.newMessageNotifcationsActor(
    patchql: PatchqlApollo,
    delayTime: Long,
    cursor: String?,
    liveNotifications: MutableLiveData<Int>
) =
    actor<NewMessageNotificationsMessage> {

        val tag = "PATCHQL_BACKGROUND"

        val processMutation = ThreadsSummaryQuery.builder().after(cursor).first(999).build()

        outer_loop@ while (true) { // iterate over incoming messages
            var msg: NewMessageNotificationsMessage? = null



            process(patchql, processMutation).await().onSuccess {
                val numNotifications = it.threads().edges().size

                if (liveNotifications.value != numNotifications) {
                    liveNotifications.postValue(numNotifications)
                }

                withTimeoutOrNull(delayTime) {
                    msg = channel.receive()
                }
            }
            when (msg) {
                is Stop -> {
                    break@outer_loop
                }
            }
        }
    }

private fun process(
    patchql: PatchqlApollo,
    query: ThreadsSummaryQuery
): CompletableDeferred<Result<ThreadsSummaryQuery.Data>> {
    val deferred = CompletableDeferred<Result<ThreadsSummaryQuery.Data>>()
    patchql.query(query) {
        deferred.complete(it.map { it.data() } as Result<ThreadsSummaryQuery.Data>)
    }
    return deferred
}
