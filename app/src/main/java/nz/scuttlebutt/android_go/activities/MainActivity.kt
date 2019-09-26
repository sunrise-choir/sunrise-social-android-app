package nz.scuttlebutt.android_go.activities


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.sunrisechoir.patchql.Params
import nz.scuttlebutt.android_go.R
import nz.scuttlebutt.android_go.databinding.ActivityMainBinding
import nz.scuttlebutt.android_go.viewModels.MainActivityViewModel
import nz.scuttlebutt.android_go.viewModels.MainActivityViewModelFactory


class MainActivity : AppCompatActivity() {

    lateinit var drawerLayout: DrawerLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        drawerLayout = binding.drawerLayout
        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(binding.navView, navController)
        NavigationUI.setupWithNavController(binding.bottomNavigation, navController)

        val notificationsBadge =
            binding.bottomNavigation.getOrCreateBadge(R.id.notifications_fragment)
        notificationsBadge.number = 12

        checkPermissions()

        val externalDir = "/sdcard"
        val repoPath = externalDir + getString(R.string.ssb_go_folder_name)
        val dbPath =
            getDatabasePath(getString(R.string.patchql_sqlite_db_name))?.absolutePath!!
        val offsetlogPath = repoPath + "/log"

        val pubKey = "@U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.ed25519"
        val privateKey = "123abc==.ed25519"

        val factory =
            MainActivityViewModelFactory(Params(offsetlogPath, dbPath, privateKey, pubKey))
        ViewModelProviders.of(this, factory).get(MainActivityViewModel::class.java)


//        val myHandlerThread: MyHandlerThread = MyHandlerThread("myHandlerThread", repoPath)
//        myHandlerThread.start()
//        val looper = myHandlerThread.looper
//        val handler = Handler(looper)

//        handler.post {
//            val aBlob: ByteArray = byteArrayOf(1,2,3)
//
//            val blobRef = Gobotexample.blobsAdd(aBlob)
//            println("The blob ref is: $blobRef")
//
//            val theRetrievedBlob = Gobotexample.blobsGet(blobRef)
//            println("The blob we got back is correct: ${theRetrievedBlob.contentEquals(aBlob)}")
//        }
//
//        handler.post{
//            val recps = Gobotexample.newRecipientsCollection()
//            Gobotexample.publish(
//                "{\"type\": \"post\", \"text\": \"I\'m a post\"}",
//                recps.marshalJSON()
//            )
//            Gobotexample.publish(
//                "{\"type\": \"contact\", \"contact\": \"@U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.ed25519\", \"following\": true}",
//                recps.marshalJSON()
//            )
//            println("done publishing some messages")
//        }


        //Gobotexample.start(repoPath)

//
//        val dbPath = this.getDatabasePath("db.sqlite").absolutePath
//        val offsetlogPath = repoPath + "/log"
//
//        val pubKey = "@U5GvOKP/YUza9k53DSXxT0mk3PIrnyAmessvNfZl5E0=.ed25519"
//        val privateKey = "123abc==.ed25519"
//
//
//        val apolloPatchql = PatchqlApollo()
//        apolloPatchql.new(
//            offsetLogPath = offsetlogPath,
//            databasePath = dbPath,
//            publicKey = pubKey,
//            privateKey = privateKey
//        )
//
//        val query = ProcessMutation.builder().chunkSize(1000000).build()
//        val threadsQuery = ThreadsQuery.builder().build()
//
//        apolloPatchql.query(query) { res -> println(res.getOrNull()?.data()) }
//        apolloPatchql.query(threadsQuery) { res -> println(res.getOrNull()?.data()) }
//
//        postButton.setOnClickListener {
//            val postEditText: EditText = findViewById(R.id.edit_post_text)
//            val publishedSeqText: TextView = findViewById(R.id.published_seq_text)
//            GlobalScope.launch {
//                val response = CompletableDeferred<Long>()
//                serverActor.send(PublishPostMessage(postEditText.text.toString(), response))
//
//                val postSeqString = response.await().toString()
//                publishedSeqText.post {
//                    postEditText.text.clear()
//                    publishedSeqText.text = postSeqString
//                    publishedSeqText.visibility = TextView.VISIBLE
//                    apolloPatchql.query(query) { res -> println(res.getOrNull()?.data())
//
//                        val postsQuery = PostsQuery.builder().last(1).build()
//                        apolloPatchql.query(postsQuery) { res -> println(res.getOrNull()?.data())}
//                    }
//                }
//            }
//        }
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            val permissions: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            requestPermissions(permissions, 0)
            // Permission is not granted
            //throw Error("no permissions for external storage")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {

            val permissions: Array<String> = arrayOf(Manifest.permission.INTERNET)
            requestPermissions(permissions, 0)
            // Permission is not granted
            //throw Error("no permissions for external storage")
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, drawerLayout)
    }


}
