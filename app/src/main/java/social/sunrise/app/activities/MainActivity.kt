package social.sunrise.app.activities


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import social.sunrise.app.R
import social.sunrise.app.databinding.ActivityMainBinding
import social.sunrise.app.viewModels.MainActivityViewModel

class MainActivity : AppCompatActivity(), KodeinAware {

    lateinit var drawerLayout: DrawerLayout
    override val kodein by kodein()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val binding =
            DataBindingUtil.setContentView<ActivityMainBinding>(this, R.layout.activity_main)
        binding.lifecycleOwner = this

        drawerLayout = binding.drawerLayout
        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(binding.navView, navController)

        val viewModel = ViewModelProviders.of(this).get(MainActivityViewModel::class.java)

        viewModel.threadNotifications.first.observe(
            this,
            observeNotification(R.id.threads_fragment, binding)
        )
        viewModel.messagesNotifications.first.observe(
            this,
            observeNotification(R.id.messages_fragment, binding)
        )
        viewModel.mentionsNotifications.first.observe(
            this,
            observeNotification(R.id.notifications_fragment, binding)
        )

        binding.bottomNavigation.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.threads_fragment -> viewModel.threadNotifications.second()
                R.id.messages_fragment -> viewModel.messagesNotifications.second()
                R.id.notifications_fragment -> viewModel.mentionsNotifications.second()
            }
            NavigationUI.onNavDestinationSelected(it, navController)
            true
        }

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

    private fun observeNotification(badgeId: Int, binding: ActivityMainBinding): Observer<Int> {
        return Observer {
            if (it > 0) {
                val notificationsBadge =
                    binding.bottomNavigation.getOrCreateBadge(badgeId)
                notificationsBadge.maxCharacterCount = 2
                notificationsBadge.number = it

            } else {
                binding.bottomNavigation.removeBadge(badgeId)
            }
        }
    }


}
