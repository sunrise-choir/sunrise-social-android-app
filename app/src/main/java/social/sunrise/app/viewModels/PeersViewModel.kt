package social.sunrise.app.viewModels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import social.sunrise.app.database.Database

class PeersViewModel(
    app: Application
) : AndroidViewModel(app), KodeinAware {
    override val kodein by kodein(app)

    private val database: Database by instance()

    val peers = database.peersDao().getAll()
}