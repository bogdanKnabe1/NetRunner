package com.ninpou.qbits

import android.app.Application
import com.ninpou.qbits.util.UtilityClass

class QbitsApp : Application() {
    override fun onCreate() {
        super.onCreate()
        UtilityClass.init(this)
    }
}