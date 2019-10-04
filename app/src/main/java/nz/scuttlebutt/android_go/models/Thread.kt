package nz.scuttlebutt.android_go.models

import androidx.recyclerview.widget.DiffUtil

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

