package social.sunrise.app.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import social.sunrise.app.R
import social.sunrise.app.databinding.FragmentProfileHolderBinding

/**
 * A simple [Fragment] subclass.
 */
class ProfileHolderFragment : Fragment() {
    private lateinit var fragmentsAdapter: FragmentsAdapter
    private lateinit var viewPager: ViewPager2
    lateinit var authorId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {


        val binding: FragmentProfileHolderBinding =
            DataBindingUtil.inflate(
                inflater,
                R.layout.fragment_profile_holder, container, false
            )

        authorId = try {
            val args = ProfileFragmentArgs.fromBundle(arguments!!)
            args.feedId!!
        } catch (e: Exception) {
            ""
        }

        fragmentsAdapter = FragmentsAdapter(this)
        viewPager = binding.pager
        viewPager.adapter = fragmentsAdapter

        TabLayoutMediator(binding.tabLayout, viewPager) { tabToSet, position ->

            when (position) {
                0 -> {
                    tabToSet.text = getString(R.string.profile_menu_item)
                    tabToSet.icon = resources.getDrawable(R.drawable.ic_person_black_24dp)

                }
                1 -> {
                    tabToSet.text = getString(R.string.posts)
                    tabToSet.icon = resources.getDrawable(R.drawable.ic_public_black_24dp)

                }
                2 -> {
                    tabToSet.text = getString(R.string.mentions)
                    tabToSet.icon = resources.getDrawable(R.drawable.ic_notifications_black_24dp)

                }
            }

        }.attach()
        return binding.root
    }
}


class FragmentsAdapter(private val parentFragment: ProfileHolderFragment) :
    FragmentStateAdapter(parentFragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {

        return when (position) {
            0 -> {
                val fragment = ProfileFragment()

                val args = ProfileFragmentArgs.Builder(parentFragment.authorId).build()
                fragment.arguments = args.toBundle()
                fragment
            }
            1 -> {
                val fragment = PostsFragment()
                val args = PostsFragmentArgs.Builder(parentFragment.authorId).build()
                fragment.arguments = args.toBundle()
                fragment
            }
            2 -> {
                val fragment = NotificationsFragment()
                val args = NotificationsFragmentArgs.Builder(parentFragment.authorId).build()
                fragment.arguments = args.toBundle()
                fragment
            }
            else -> {
                NotificationsFragment()
            }
        }

    }
}

