package com.gamepackage.codereview.activities

import android.app.Application
import com.gamepackage.codereview.utils.MSPV3
import com.gamepackage.codereview.utils.MySignal

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        MSPV3.init(this)
        MySignal.init(this)
    }
}