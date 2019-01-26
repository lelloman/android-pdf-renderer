package com.lelloman.pdfrenderer.demo

import android.app.Activity
import android.app.Application
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

open class DemoApplication : Application() {

    protected open fun provideControlsViewPdfViewActivityViewModel(activity: AppCompatActivity) : PdfViewViewModel =
        ViewModelProviders.of(activity, null).get(PdfViewViewModel::class.java)

    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity?, savedInstanceState: Bundle?) {
                if(activity is PdfViewActivity) {
                    activity.viewModel = provideControlsViewPdfViewActivityViewModel(activity)
                }
            }

            override fun onActivityStarted(activity: Activity?) {
            }

            override fun onActivityResumed(activity: Activity?) {
            }

            override fun onActivityPaused(activity: Activity?) {
            }

            override fun onActivityStopped(activity: Activity?) {
            }

            override fun onActivitySaveInstanceState(activity: Activity?, outState: Bundle?) {
            }

            override fun onActivityDestroyed(activity: Activity?) {
            }
        })
    }
}