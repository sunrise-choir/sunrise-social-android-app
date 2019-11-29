package social.sunrise.app


import android.app.Application
import android.content.Context
import androidx.navigation.findNavController
import com.sunrisechoir.patchql.Params
import com.sunrisechoir.patchql.PatchqlApollo
import gobotexample.Gobotexample
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
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.generic.*
import social.sunrise.app.database.Database
import social.sunrise.app.models.PatchqlBackgroundMessage
import social.sunrise.app.utils.SsbUri
import java.io.File


@Serializable
data class Secret(val id: String, val private: String, val curve: String, val public: String)


class ScuttlebuttApp : Application(), KodeinAware {

    override val kodein: Kodein by Kodein.lazy {

        val externalDir = getDir("scuttlebutt", Context.MODE_PRIVATE).absolutePath
        val repoPath = externalDir + getString(R.string.ssb_go_folder_name)
        val dbPath =
            getDatabasePath(getString(R.string.patchql_sqlite_db_name))?.absolutePath!!
        val offsetlogPath = repoPath + "/log"

        //We won't have these on first startup
        val secretFile = File(repoPath + "/secret")

        //This is an ugly fix so that if the secrets file is empty we start and stop the server.
        //This creates the secret file.
        try {
            Json.parse(Secret.serializer(), secretFile.readText())
        } catch (e: Throwable) {
            Gobotexample.start(repoPath)
            Gobotexample.stop()
        }

        val secrets = Json.parse(Secret.serializer(), secretFile.readText())
        val pubKey = secrets.id
        val privateKey = secrets.private

        println("loaded pubkey: $pubKey from file.")

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
                                    NavigationDirections.actionGlobalProfileHolderFragment(
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




}