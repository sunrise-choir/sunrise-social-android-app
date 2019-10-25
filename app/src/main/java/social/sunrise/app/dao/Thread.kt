package social.sunrise.app.dao

import androidx.lifecycle.LiveData
import androidx.paging.DataSource
import com.sunrisechoir.graphql.ThreadForPostQuery
import social.sunrise.app.models.Post

//TODO this won't work yet.
// It would be cool if thread had paginated replies but it doesn't. This means making live data here isn't quite right. tbd.
interface Thread {
    fun reload(rootId: String)
    fun getAllPaged(query: () -> ThreadForPostQuery.Builder): DataSource.Factory<String, LiveData<Post>>
    fun like(postId: String, doesLike: Boolean)
    //fun reply(rootId: String, replyText: String, mentions: List<String>, channels: List<String>)
}