package nz.scuttlebutt.android_go

//import android.app.Activity
//import android.app.Application
//import nz.scuttlebutt.android_go.di.AppInjector
//import dagger.android.DispatchingAndroidInjector
//import dagger.android.HasActivityInjector
//
//import javax.inject.Inject
//
//
//class ScuttlebuttApp : Application(), HasActivityInjector {
//    @Inject
//    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Activity>
//
//    override fun onCreate() {
//        super.onCreate()
//
//        AppInjector.init(this)
//    }
//
//    override fun activityInjector() = dispatchingAndroidInjector
//}