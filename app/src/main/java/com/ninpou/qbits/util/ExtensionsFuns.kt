package com.ninpou.qbits.util

import android.view.View
import android.widget.Toast

fun showShortToast(message: String) {
    Toast.makeText(APP_ACTIVITY, message, Toast.LENGTH_SHORT).show()
}

fun View.hideViews() {
    visibility = View.GONE
}

fun hideViews(vararg views: View) {
    views.forEach { it.hideViews() }
}

fun View.showViews() {
    visibility = View.VISIBLE
}

fun showViews(vararg views: View) {
    views.forEach { it.showViews() }
}