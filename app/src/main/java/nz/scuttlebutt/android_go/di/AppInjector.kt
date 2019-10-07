package nz.scuttlebutt.android_go.di

//import dagger.android.support.HasSupportFragmentInjector
//import nz.scuttlebutt.android_go.ScuttlebuttApp

/**
 * Helper class to automatically inject fragments if they implement [Injectable].
 */
//object AppInjector {
//    fun init(ssbApp: ScuttlebuttApp) {
//        DaggerAppComponent.builder().application(ssbApp)
//            .build().inject(ssbApp)
//        ssbApp
//            .registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
//                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
//                    handleActivity(activity)
//                }
//
//                override fun onActivityStarted(activity: Activity) {
//
//                }
//
//                override fun onActivityResumed(activity: Activity) {
//
//                }
//
//                override fun onActivityPaused(activity: Activity) {
//
//                }
//
//                override fun onActivityStopped(activity: Activity) {
//
//                }
//
//                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle?) {
//
//                }
//
//                override fun onActivityDestroyed(activity: Activity) {
//
//                }
//            })
//    }
//
//    private fun handleActivity(activity: Activity) {
//        if (activity is HasSupportFragmentInjector) {
//            AndroidInjection.inject(activity)
//        }
//        if (activity is FragmentActivity) {
//            activity.supportFragmentManager
//                .registerFragmentLifecycleCallbacks(
//                    object : FragmentManager.FragmentLifecycleCallbacks() {
//                        override fun onFragmentCreated(
//                            fm: FragmentManager,
//                            f: Fragment,
//                            savedInstanceState: Bundle?
//                        ) {
//                            if (f is Injectable) {
//                                AndroidSupportInjection.inject(f)
//                            }
//                        }
//                    }, true
//                )
//        }
//    }
//}