package nz.scuttlebutt.android_go


import android.Manifest
import android.app.Activity
import android.app.Application
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import com.sunrisechoir.patchql.Params
import com.sunrisechoir.patchql.PatchqlApollo
import io.noties.markwon.AbstractMarkwonPlugin
import io.noties.markwon.LinkResolverDef
import io.noties.markwon.Markwon
import io.noties.markwon.MarkwonConfiguration
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.channels.SendChannel
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.content
import nz.scuttlebutt.android_go.database.Database
import nz.scuttlebutt.android_go.models.PatchqlBackgroundMessage
import nz.scuttlebutt.android_go.utils.SsbUri
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.*
import java.io.File


@Serializable
data class Secret(val id: String, val private: String, val curve: String, val public: String)



class ScuttlebuttApp : Application(), KodeinAware {

    override val kodein: Kodein by Kodein.lazy {
        checkPermissions()
        val externalDir = "/sdcard"
        val repoPath = externalDir + getString(R.string.ssb_go_folder_name)
        val dbPath =
            getDatabasePath(getString(R.string.patchql_sqlite_db_name))?.absolutePath!!
        val offsetlogPath = repoPath + "/log"

        // Remove me!
        //Gobotexample.start(repoPath)

        //We won't have these on first startup
        val secretFile = File(repoPath + "/secret")

        val secrets = Json.parse(Secret.serializer(), secretFile.readText())
        val pubKey = secrets.id
        val privateKey = secrets.private



        bind<CompletableDeferred<SendChannel<SsbServerMsg>>>("ssbServerActor") with singleton { CompletableDeferred<SendChannel<SsbServerMsg>>() }
        bind<CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>>("patchqlProcessActor") with singleton { CompletableDeferred<SendChannel<PatchqlBackgroundMessage>>() }

        //We should check if the offset file exists yet (it won't if this if the first startup)
        bind<PatchqlApollo>() with singleton {
            PatchqlApollo(
                Params(
                    offsetlogPath,
                    dbPath,
                    pubKey,
                    privateKey
                )
            )
        }

        bind<Database>() with provider {
            Database(
                instance(),
                instance("ssbServerActor"),
                instance("patchqlProcessActor")
            )
        }

        val mdToEmoji: JsonObject =
            Json.parse(JsonObject.serializer(), getString(R.string.mdToEmoji))
        val mdRegex = Regex(":(\\w+):")
        val ssbLinkRefNotInMarkdwonLinkRegex =
            Regex("[^\\(](([@%&])([A-Za-z0-9/+]{43}=)\\.([\\w\\d]+))[^\\)]")

        bind<Markwon>() with singleton {
            val plugin = object : AbstractMarkwonPlugin() {
                override fun processMarkdown(markdown: String): String {
                    return markdown.replace(mdRegex) {
                        val jsonEmoji = mdToEmoji[it.groups[1]?.value]
                        val emoji = jsonEmoji?.content ?: it.value
                        emoji
                    }.replace(ssbLinkRefNotInMarkdwonLinkRegex) {
                        val (_, match) = it.groupValues
                        val link = SsbUri.fromSigilLink(match)
                        if (link.isMessage() || link.isFeed()) {
                            "[${match}](${match})"
                        } else {
                            it.value
                        }
                    }
                }

                override fun configureConfiguration(builder: MarkwonConfiguration.Builder) {
                    super.configureConfiguration(builder)


                    builder.linkResolver { view, link ->

                        if (SsbUri.isSsbRef(link)) {
                            val navController = view.findNavController()

                            val uri = SsbUri.fromSigilLink(link)
                            if (uri.isMessage()) {
                                navController.navigate(
                                    NavigationDirections.actionGlobalThreadFragment(
                                        null,
                                        link
                                    )
                                )
                            } else if (uri.isFeed()) {
                                navController.navigate(
                                    NavigationDirections.actionGlobalProfileFragment(
                                        link
                                    )
                                )
                            }
                            //TODO: Channels, when patchql supports them.

                        } else {
                            LinkResolverDef().resolve(view, link)
                        }
                    }

                }
            }
            Markwon.builder(applicationContext)
                .usePlugin(plugin)
                .build()
        }

        constant("repoPath") with repoPath
        constant("mySsbIdentity") with pubKey

    }


    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {

            val permissions: Array<String> = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            ActivityCompat.requestPermissions(applicationContext as Activity, permissions, 0)
            //requestPermissions(permissions, 0)
            // Permission is not granted
            //throw Error("no permissions for external storage")
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
            != PackageManager.PERMISSION_GRANTED
        ) {

            val permissions: Array<String> = arrayOf(Manifest.permission.INTERNET)
            ActivityCompat.requestPermissions(applicationContext as Activity, permissions, 0)
            //requestPermissions(permissions, 0)
            // Permission is not granted
            //throw Error("no permissions for external storage")
        }
    }

}