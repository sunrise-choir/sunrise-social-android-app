package nz.scuttlebutt.android_go.viewModels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.sunrisechoir.patchql.Params

class MainActivityViewModelFactory(
    private val patchqlParams: Params

) : ViewModelProvider.Factory {
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        @Suppress("UNCHECKED_CAST")
        return MainActivityViewModel(patchqlParams) as T
    }
}