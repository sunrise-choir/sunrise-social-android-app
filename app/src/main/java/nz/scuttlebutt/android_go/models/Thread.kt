package nz.scuttlebutt.android_go.models

import androidx.recyclerview.widget.DiffUtil

data class Thread(val root: Post, val repliesLength: String, val cursor: String) {
    companion object {
        var DIFF_CALLBACK: DiffUtil.ItemCallback<Thread> =
            object : DiffUtil.ItemCallback<Thread>() {
                override fun areItemsTheSame(oldItem: Thread, newItem: Thread): Boolean {
                    return oldItem.cursor === newItem.cursor
                }

                override fun areContentsTheSame(oldItem: Thread, newItem: Thread): Boolean {
                    return oldItem.equals(newItem)
                }
            }
    }
}

