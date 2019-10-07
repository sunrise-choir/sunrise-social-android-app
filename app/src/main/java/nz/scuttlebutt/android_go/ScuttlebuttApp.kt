package nz.scuttlebutt.android_go


import android.app.Application
import com.sunrisechoir.patchql.Params
import com.sunrisechoir.patchql.PatchqlApollo
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.json.Json
import nz.scuttlebutt.android_go.activities.Secret
import nz.scuttlebutt.android_go.database.Database
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.*
import java.io.File


class ScuttlebuttApp : Application(), KodeinAware {

    override val kodein: Kodein by Kodein.lazy {
        val externalDir = "/sdcard"
        val repoPath = externalDir + getString(R.string.ssb_go_folder_name)
        val dbPath =
            getDatabasePath(getString(R.string.patchql_sqlite_db_name))?.absolutePath!!
        val offsetlogPath = repoPath + "/log"

        val secretFile = File(repoPath + "/secret")

        val secrets = Json.parse(Secret.serializer(), secretFile.readText())
        val pubKey = secrets.id
        val privateKey = secrets.private

        bind<PatchqlApollo>() with singleton {
            PatchqlApollo(
                Params(
                    offsetlogPath,
                    dbPath,
                    privateKey,
                    pubKey
                )
            )
        }

        bind<CompletableDeferred<SendChannel<SsbServerMsg>>>("ssbServerActor") with singleton { CompletableDeferred<SendChannel<SsbServerMsg>>() }
        bind<CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>>("patchqlProcessActor") with singleton { CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>() }

        bind<Database>() with provider {
            Database(
                instance(),
                instance("ssbServerActor"),
                instance("patchqlProcessActor")
            )
        }

        constant("repoPath") with repoPath
    }

}