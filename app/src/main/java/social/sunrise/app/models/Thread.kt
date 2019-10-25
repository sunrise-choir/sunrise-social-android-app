package social.sunrise.app.models

import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.DiffUtil

typealias LiveThread = LiveData<Thread>

val LIVE_THREAD_DIFF_CALLBACK = object : DiffUtil.ItemCallback<LiveThread>() {
    override fun areItemsTheSame(
        oldItem: LiveThread,
        newItem: LiveThread
    ): Boolean {
        return oldItem.value?.root!!.id === newItem.value?.root!!.id
    }

    override fun areContentsTheSame(
        oldItem: LiveThread,
        newItem: LiveThread
    ): Boolean {
        return oldItem.equals(newItem)
    }
}

data class Thread(val root: Post, val cursor: String) {
    companion object {
        var DIFF_CALLBACK: DiffUtil.ItemCallback<Thread> =
            object : DiffUtil.ItemCallback<Thread>() {
                override fun areItemsTheSame(oldItem: Thread, newItem: Thread): Boolean {
                    val isTheSame =  oldItem.cursor == newItem.cursor
                    if(isTheSame){
                        println("is the same")
                    }
                    return isTheSame
                }

                override fun areContentsTheSame(oldItem: Thread, newItem: Thread): Boolean {
                    return oldItem.equals(newItem)
                }
            }
    }

}

