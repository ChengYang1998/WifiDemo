package com.android.wifidemo

import android.app.Application
import com.hjq.toast.Toaster


  class MyApplication : Application {
     constructor()

      private lateinit var mApplication: MyApplication

      fun getInstance(): MyApplication? {
          return mApplication
      }

    override fun onCreate() {
        super.onCreate()
        mApplication = this
        // 初始化 Toast 框架
        Toaster.init(this)
    }
}