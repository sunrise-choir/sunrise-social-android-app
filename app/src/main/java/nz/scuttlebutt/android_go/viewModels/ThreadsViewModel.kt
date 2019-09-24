package nz.scuttlebutt.android_go.viewModels


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sunrisechoir.graphql.ThreadsSummaryQuery.Node
import com.sunrisechoir.patchql.PatchqlApollo


class ThreadsViewModel: ViewModel() {

    lateinit var patchqlApollo: PatchqlApollo

    init{





//        patchqlApollo = PatchqlApollo()
//        patchqlApollo.new(
//            offsetLogPath = offsetlogPath,
//            databasePath = dbPath!!,
//            publicKey = pubKey,
//            privateKey = privateKey
//        )
    }

    private val nodes: MutableLiveData<List<Node>> by lazy {
        MutableLiveData<List<Node>>().also {
            loadNodes()
        }
    }

    fun getNodes(): LiveData<List<Node>> {
        return nodes
    }

    private fun loadNodes() {
        // Do an asynchronous operation to fetch users.
    }

}