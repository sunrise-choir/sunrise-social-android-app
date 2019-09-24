package nz.scuttlebutt.android_go.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sunrisechoir.patchql.Patchql

class ThreadsViewModelFactory(val patchqlParams: Patchql.Params) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return ThreadsViewModel(patchqlParams) as T
    }
}