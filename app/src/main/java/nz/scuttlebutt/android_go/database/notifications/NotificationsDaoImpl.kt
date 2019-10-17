package nz.scuttlebutt.android_go.database.notifications

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.sunrisechoir.graphql.PostsQuery
import com.sunrisechoir.graphql.ThreadsSummaryQuery
import com.sunrisechoir.patchql.PatchqlApollo
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import io.reactivex.subjects.AsyncSubject
import java.util.concurrent.TimeUnit
import nz.scuttlebutt.android_go.dao.Notifications as NotificationsDao

class NotificationsDaoImpl(private val patchqlApollo: PatchqlApollo) : NotificationsDao {
    override fun getThreadsNotifications(queryBuilder: () -> ThreadsSummaryQuery.Builder): Pair<LiveData<Int>, () -> Unit> {

        val liveNotifications: MutableLiveData<Int> = MutableLiveData(0)
        var disposable: Disposable? = null

        val reset: () -> Unit = {
            liveNotifications.postValue(0)

            val currentCursor: String? = null
            val asyncSubject: Observable<String> = AsyncSubject.create { obs ->
                val query = queryBuilder().before(currentCursor).last(1).build()
                patchqlApollo.query(query) {
                    it.map {
                        it.data() as ThreadsSummaryQuery.Data
                    }.map {
                        it.threads().edges().first().cursor()
                    }.onSuccess {
                        obs.onNext(it.orEmpty())
                    }
                }
            }

            if (disposable != null)
                disposable?.dispose()

            disposable = asyncSubject
                .observeOn(Schedulers.newThread())
                .flatMap { cursor ->
                    //Rx doesn't let us emit a null value :(
                    //So we're going to treat the empty string as null
                    //Ugh this makes me sad
                    val nullableCursor: String? = if (cursor.isEmpty()) null else cursor

                    val query = queryBuilder().after(nullableCursor).first(999).build()

                    Observable.interval(2000, TimeUnit.MILLISECONDS)
                        .map {
                            patchqlApollo.query(query)
                        }
                }
                .map {
                    it.data() as ThreadsSummaryQuery.Data
                }
                .map {
                    it.threads().edges().size
                }
                .distinctUntilChanged()
                .subscribe { t ->
                    liveNotifications.postValue(t)
                }
        }
        reset()

        return Pair(liveNotifications, reset)
    }

    override fun getPostsNotifications(queryBuilder: () -> PostsQuery.Builder): Pair<LiveData<Int>, () -> Unit> {
        val liveNotifications: MutableLiveData<Int> = MutableLiveData(0)
        var disposable: Disposable? = null

        val reset: () -> Unit = {
            val currentCursor: String? = null
            val asyncSubject: Observable<String> = AsyncSubject.create { obs ->
                val query = queryBuilder().before(currentCursor).last(1).build()
                patchqlApollo.query(query) {
                    it.map {
                        it.data() as PostsQuery.Data
                    }.map {
                        it.posts().edges().first().cursor()
                    }.onSuccess {
                        obs.onNext(it.orEmpty())
                    }
                }
            }

            if (disposable != null)
                disposable?.dispose()

            disposable = asyncSubject
                .observeOn(Schedulers.newThread())
                .flatMap { cursor ->
                    //Rx doesn't let us emit a null value :(
                    //So we're going to treat the empty string as null
                    //Ugh this makes me sad
                    val nullableCursor: String? = if (cursor.isEmpty()) null else cursor

                    val query = queryBuilder().after(nullableCursor).first(
                        99999
                    ).build()

                    Observable.interval(2000, TimeUnit.MILLISECONDS)
                        .map {
                            patchqlApollo.query(query)
                        }
                }
                .map {
                    it.data() as PostsQuery.Data
                }
                .map {
                    it.posts().edges().size
                }
                .distinctUntilChanged()
                .subscribe { t ->
                    liveNotifications.postValue(t)
                }

        }

        return Pair(liveNotifications, reset)
    }
}