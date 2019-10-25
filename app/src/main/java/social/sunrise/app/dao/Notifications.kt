package social.sunrise.app.dao

import androidx.lifecycle.LiveData
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.graphql.ThreadsSummaryQuery


interface Notifications {
    fun getThreadsNotifications(query: () -> ThreadsSummaryQuery.Builder): Pair<LiveData<Int>, () -> Unit>
    fun getPostsNotifications(query: () -> PostsQuery.Builder): Pair<LiveData<Int>, () -> Unit>
}