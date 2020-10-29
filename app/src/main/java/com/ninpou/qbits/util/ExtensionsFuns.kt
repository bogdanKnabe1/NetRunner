package com.ninpou.qbits.util

import android.widget.Toast

fun showShortToast(message: String) {
    Toast.makeText(APP_ACTIVITY, message, Toast.LENGTH_SHORT).show()
}

//check match of regex
fun String.matches(regex: String): Boolean {
    throw RuntimeException("Stub!")
}