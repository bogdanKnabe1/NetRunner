package com.ninpou.qbits.util

import android.widget.Toast

fun showLongToast(message: String) {
    Toast.makeText(APP_ACTIVITY, message, Toast.LENGTH_LONG).show()
}