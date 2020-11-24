package com.b_knabe.net_runner.util

import android.content.Context

class ResourceCompare {

    fun isEqual(context: Context, resId:Int, string: String):Boolean {
        return context.getString(resId) == string
    }
}